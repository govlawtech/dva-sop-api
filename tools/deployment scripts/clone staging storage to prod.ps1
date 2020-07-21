$SourceStorageContext = (Get-AzStorageAccount -ResourceGroupName govlawtechmainresourcegroup -Name dvasopapistoragedevtest).Context
$DestStorageContext =  (Get-AzStorageAccount -ResourceGroupName govlawtechmainresourcegroup -Name dvasopapistorage).Context

$DestinationContainersToDelete = Get-AzStorageContainer -Context $DestStorageContext 

foreach ($SingleDestinationContainerToDelete in $DestinationContainersToDelete)
{
    Remove-AzStorageContainer -Name $SingleDestinationContainerToDelete.Name -Context $SingleDestinationContainerToDelete.Context -Force
    Write-Output "Removed destination storage container: $SingleDestinationContainerToDelete."
}

Start-Sleep -Seconds 30

$SourceContainers = Get-AzStorageContainer -Context $SourceStorageContext | Where-Object {!$_.Name.StartsWith("$")}

foreach($SingleSourceContainer in $SourceContainers)
{
    $SingleSourceContainerName = $SingleSourceContainer.Name
    Write-Output "Creating new target container '$SingleSourceContainerName'."
    New-AzStorageContainer -Name $SingleSourceContainerName -Permission Off -Context $DestStorageContext -ErrorAction Stop 

    
    $Blobs = Get-AzStorageBlob -Context $SourceStorageContext -Container $SingleSourceContainerName
    $BlobCpyAry = @() #Create array of objects

    #Do the copy of everything
    foreach ($Blob in $Blobs)
    {
       $BlobName = $Blob.Name
       Write-Output "Copying $BlobName from $SingleSourceContainerName"
       $BlobCopy = Start-AzStorageBlobCopy -Context $SourceStorageContext -SrcContainer $SingleSourceContainerName -SrcBlob $BlobName -DestContext $DestStorageContext -DestContainer $SingleSourceContainerName -DestBlob $BlobName -Force
       $BlobCpyAry += $BlobCopy
    }

    #Check Status
    foreach ($BlobCopy in $BlobCpyAry)
    {
      
       $CopyState = $BlobCopy | Get-AzStorageBlobCopyState
       $Message = $CopyState.Source.AbsolutePath + " " + $CopyState.Status + " {0:N2}%" -f (($CopyState.BytesCopied/$CopyState.TotalBytes)*100) 
       Write-Output $Message
    }
}

