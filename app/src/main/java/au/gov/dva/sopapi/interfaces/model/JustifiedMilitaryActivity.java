package au.gov.dva.sopapi.interfaces.model;

import java.util.List;



public class JustifiedMilitaryActivity {
    private final MilitaryActivity militaryActivity;
    private final List<Deployment> relevantDeployments;
    public JustifiedMilitaryActivity(MilitaryActivity militaryActivity, List<Deployment> relevantDeployments)
    {
        this.militaryActivity = militaryActivity;
        this.relevantDeployments = relevantDeployments;
    }

    public MilitaryActivity getMilitaryActivity() {
        return militaryActivity;
    }

    public List<Deployment> getRelevantDeployments() {
        return relevantDeployments;
    }
}
