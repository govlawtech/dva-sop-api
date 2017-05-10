package au.gov.dva.sopapi.sopref.data.updates;

import au.gov.dva.sopapi.AppSettings;
import au.gov.dva.sopapi.exceptions.AutoUpdateError;
import au.gov.dva.sopapi.interfaces.model.LegislationRegisterEmailUpdate;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LegislationRegisterEmailUpdates {

    static Logger logger = LoggerFactory.getLogger(LegislationRegisterEmailUpdate.class);

    public static CompletableFuture<ImmutableSet<LegislationRegisterEmailUpdate>> getEmailsReceivedBetween(OffsetDateTime startDateExclusive, OffsetDateTime endDateExclusive, String senderAddress) {

        CompletableFuture<ImmutableSet<LegislationRegisterEmailUpdate>> future = CompletableFuture.supplyAsync(new Supplier<ImmutableSet<LegislationRegisterEmailUpdate>>() {
            @Override
            public ImmutableSet<LegislationRegisterEmailUpdate> get() {
                return ImmutableSet.copyOf(getUpdatesBetween(startDateExclusive, endDateExclusive, senderAddress));
            }
        });

        return future;
    }


    private static Set<LegislationRegisterEmailUpdate> getUpdatesBetween(OffsetDateTime startDateExclusive,
                                                                         OffsetDateTime endDateExclusive,
                                                                         String senderAddress) {

        String emailAddress = AppSettings.LegislationRegisterEmailSubscription.getUserId();
        String emailPassword = AppSettings.LegislationRegisterEmailSubscription.getPassword();

        Properties props = new Properties();

        try {
            InputStream propertiesStream = LegislationRegisterEmailUpdates.class.getClassLoader().getResourceAsStream("smtp.properties");
            props.load(propertiesStream);
            Session emailSession = Session.getDefaultInstance(props, null);
            Store store = emailSession.getStore("imaps");
            store.connect("smtp.gmail.com", emailAddress, emailPassword);

            Folder inbox = store.getFolder("inbox");
            inbox.open(Folder.READ_ONLY);

            Stream<LegislationRegisterEmailUpdate> updates = Arrays.stream(inbox.getMessages())
                    .filter(m -> m instanceof MimeMessage)
                    .filter(m -> {
                        try {
                            Address sender = ((MimeMessage) m).getSender();
                            return sender.toString().contains(senderAddress);
                        } catch (MessagingException e) {
                            throw new AutoUpdateError(e);
                        }
                    })
                    .filter(m -> {
                        try {
                            OffsetDateTime sentDate = emailDateToOdt(m.getSentDate());
                            return sentDate.isAfter(startDateExclusive) && sentDate.isBefore(endDateExclusive);
                        } catch (MessagingException e) {
                            throw new AutoUpdateError(e);
                        }
                    })
                    .flatMap(m -> {
                        try {
                            return parseMessage(m, emailDateToOdt(m.getSentDate())).stream();
                        } catch (IOException e) {
                            throw new AutoUpdateError(e);
                        } catch (MessagingException e) {
                            throw new AutoUpdateError(e);
                        }
                    })
                    .distinct();

            return updates.collect(Collectors.toSet());

        } catch (IOException ex) {
            throw new AutoUpdateError(ex);
        } catch (MessagingException ex) {
            throw new AutoUpdateError(ex);
        }

    }


    private static OffsetDateTime emailDateToOdt(Date date) {
        return OffsetDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
    }

    private static Set<LegislationRegisterEmailUpdate> parseMessage(Message msg, OffsetDateTime sentDate) throws IOException, MessagingException {
        Object content = msg.getContent();
        if (content instanceof MimeMultipart) {
            MimeMultipart mimeMultipartContent = (MimeMultipart) content;
            for (int i = 0; i < mimeMultipartContent.getCount(); i++) {
                BodyPart part = mimeMultipartContent.getBodyPart(i);
                if (part.getContentType().contains("TEXT/HTML")) {
                    String msgContent = (String) part.getContent();

                    int legislativeInstrumentsStart = msgContent.indexOf("Legislative Instruments");
                    int legislativeInstrumentCompilationsStart = msgContent.indexOf("Legislative Instrument Compilations");
                    int subscriptionDetailsStart = msgContent.indexOf("SUBSCRIPTION DETAILS");

                    String legislativeInstrumentsSection = null;
                    String legislativeInstrumentCompilationsSection = null;

                    if (legislativeInstrumentsStart != -1) {
                        if (legislativeInstrumentCompilationsStart != -1) {
                            legislativeInstrumentsSection = msgContent.substring(legislativeInstrumentsStart, legislativeInstrumentCompilationsStart);
                        } else {
                            legislativeInstrumentsSection = msgContent.substring(legislativeInstrumentsStart, subscriptionDetailsStart);
                        }
                    }

                    if (legislativeInstrumentCompilationsStart != -1) {
                        legislativeInstrumentCompilationsSection = msgContent.substring(legislativeInstrumentCompilationsStart, subscriptionDetailsStart);
                    }

                    List<LegislationRegisterEmailUpdate> instrumentUpdates = new ArrayList<>();
                    List<LegislationRegisterEmailUpdate> compilationUpdates = new ArrayList<>();

                    if (legislativeInstrumentsSection != null) {
                        instrumentUpdates = processLegislativeInstruments(legislativeInstrumentsSection, sentDate);
                    }

                    if (legislativeInstrumentCompilationsSection != null) {
                        compilationUpdates = processLegislativeInstrumentCompilations(legislativeInstrumentCompilationsSection, sentDate);
                    }

                    Set<LegislationRegisterEmailUpdate> updatesSet = new HashSet<>();
                    updatesSet.addAll(instrumentUpdates);
                    updatesSet.addAll(compilationUpdates);

                    return updatesSet;
                }
            }
        }

        logger.error(String.format("Message not MIME multipart, cannot parse: %s.", msg));

        return ImmutableSet.of();
    }

    private static List<LegislationRegisterEmailUpdate> processLegislativeInstruments(String legislativeInstrumentsSection, OffsetDateTime sentDate) {
        String[] lines = legislativeInstrumentsSection.split("<br>");
        // discard the header lines
        lines = Arrays.copyOfRange(lines, 2, lines.length);

        List<LegislationRegisterEmailUpdate> updates = new ArrayList<>();
        int currentUpdateLineNumber = -1;

        Pattern urlMatchPattern = Pattern.compile("a href=\"([^\"]+)\"");

        LegislationRegisterEmailUpdateImpl currentUpdate = null;
        for (String line : lines) {
            if (line.equals("")) {
                if (currentUpdate != null) {
                    currentUpdate = null;
                    currentUpdateLineNumber = -1;
                }
            } else {
                if (currentUpdateLineNumber == -1) {
                    currentUpdateLineNumber = 1;
                    currentUpdate = new LegislationRegisterEmailUpdateImpl();
                    updates.add(currentUpdate);
                    currentUpdate.setDateReceived(sentDate);
                } else {
                    currentUpdateLineNumber++;
                }

                if (currentUpdateLineNumber == 1) {
                    currentUpdate.setInstrumentTitle(line);
                } else if (currentUpdateLineNumber == 2) {
                    // discard
                } else if (currentUpdateLineNumber == 3) {
                    currentUpdate.setUpdateDescription(line);
                } else if (currentUpdateLineNumber == 4) {
                    Matcher urlMatcher = urlMatchPattern.matcher(line);
                    if (urlMatcher.find()) {
                        String strUrl = urlMatcher.group(1);

                        try {
                            currentUpdate.setRegisterLink(new URL(strUrl));
                        } catch (MalformedURLException ex) {
                            logger.error(strUrl, ex);
                        }
                    }
                } else {
                    logger.error(String.format("Invalid number of lines for instrument update: %s.", legislativeInstrumentsSection));
                }
            }
        }

        return updates;
    }

    private static List<LegislationRegisterEmailUpdate> processLegislativeInstrumentCompilations(String legislativeInstrumentCompilationsSection, OffsetDateTime sentDate) {
        String[] lines = legislativeInstrumentCompilationsSection.split("<br>");
        // discard the header lines
        lines = Arrays.copyOfRange(lines, 2, lines.length);

        List<LegislationRegisterEmailUpdate> updates = new ArrayList<>();
        int currentUpdateLineNumber = -1;

        Pattern urlMatchPattern = Pattern.compile("a href=\"([^\"]+)\"");

        LegislationRegisterEmailUpdateImpl currentUpdate = null;
        for (String line : lines) {
            if (line.equals("")) {
                if (currentUpdate != null) {
                    currentUpdate = null;
                    currentUpdateLineNumber = -1;
                }
            } else {
                if (currentUpdateLineNumber == -1) {
                    currentUpdateLineNumber = 1;
                    currentUpdate = new LegislationRegisterEmailUpdateImpl();
                    updates.add(currentUpdate);
                    currentUpdate.setDateReceived(sentDate);
                } else {
                    currentUpdateLineNumber++;
                }

                if (currentUpdateLineNumber == 1) {
                    currentUpdate.setInstrumentTitle(line);
                } else if (currentUpdateLineNumber == 2) {
                    currentUpdate.setUpdateDescription(line);
                } else if (currentUpdateLineNumber == 3) {
                    Matcher urlMatcher = urlMatchPattern.matcher(line);
                    if (urlMatcher.find()) {
                        String strUrl = urlMatcher.group(1);

                        try {
                            currentUpdate.setRegisterLink(new URL(strUrl));
                        } catch (MalformedURLException ex) {
                            logger.error(strUrl, ex);
                        }
                    }
                } else {
                    logger.error(String.format("Invalid number of lines for instrument update: %s.", legislativeInstrumentCompilationsSection));
                }
            }
        }

        return updates;
    }

    public static class LegislationRegisterEmailUpdateImpl implements LegislationRegisterEmailUpdate {

        private String instrumentTitle;
        private String updateDescription;
        private URL registerLink;
        private OffsetDateTime dateReceived;

        @Override
        public String getInstrumentTitle() {
            assert (instrumentTitle != null);
            return instrumentTitle;
        }

        @Override
        public String getUpdateDescription() {
            assert (updateDescription != null);
            return updateDescription;
        }

        @Override
        public URL getRegisterLink() {
            assert (registerLink != null);
            return registerLink;
        }

        @Override
        public OffsetDateTime getDateReceived() {
            assert (dateReceived != null);
            return dateReceived;
        }

        public void setInstrumentTitle(String instrumentTitle) {
            this.instrumentTitle = instrumentTitle;
        }


        public void setUpdateDescription(String updateDescription) {
            this.updateDescription = updateDescription;
        }

        public void setRegisterLink(URL registerLink) {
            this.registerLink = registerLink;
        }

        public void setDateReceived(OffsetDateTime dateReceived) {
            this.dateReceived = dateReceived;
        }

        @Override
        public String toString() {
            return "LegislationRegisterEmailUpdateImpl{" +
                    "instrumentTitle='" + instrumentTitle + '\'' +
                    ", updateDescription='" + updateDescription + '\'' +
                    ", registerLink=" + registerLink +
                    ", dateReceived=" + dateReceived +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LegislationRegisterEmailUpdateImpl that = (LegislationRegisterEmailUpdateImpl) o;
            return com.google.common.base.Objects.equal(getInstrumentTitle(), that.getInstrumentTitle()) &&
                    Objects.equal(getUpdateDescription(), that.getUpdateDescription()) &&
                    Objects.equal(getRegisterLink(), that.getRegisterLink()) &&
                    Objects.equal(getDateReceived(), that.getDateReceived());
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(getInstrumentTitle(), getUpdateDescription(), getRegisterLink(), getDateReceived());
        }
    }
}
