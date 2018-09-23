Param(
    [Parameter(Mandatory = $true)]
    [string]$SourceStorageAccount,

    [Parameter(Mandatory = $true)]
    [string]$SourceStorageKey,

    [Parameter(Mandatory = $true)]
    [string]$DestStorageAccount,

    [Parameter(Mandatory = $true)]
    [string]$DestStorageKey
)


$SourceStorageContext = New-AzureStorageContext -StorageAccountName $SourceStorageAccount -StorageAccountKey $SourceStorageKey
$DestStorageContext = New-AzureStorageContext -StorageAccountName $DestStorageAccount -StorageAccountKey $DestStorageKey

$DestinationContainersToDelete = Get-AzureStorageContainer -Context $DestStorageContext 

foreach ($SingleDestinationContainerToDelete in $DestinationContainersToDelete)
{
    Remove-AzureStorageContainer -Name $SingleDestinationContainerToDelete.Name -Context $SingleDestinationContainerToDelete.Context -Force
    Write-Output "Removed destination storage container: $SingleDestinationContainerToDelete."
}

Start-Sleep -Seconds 15

$SourceContainers = Get-AzureStorageContainer -Context $SourceStorageContext | Where-Object {!$_.Name.StartsWith("$")}

foreach($SingleSourceContainer in $SourceContainers)
{
    $SingleSourceContainerName = $SingleSourceContainer.Name
    if (!((Get-AzureStorageContainer -Context $DestStorageContext) | Where-Object { $_.Name -eq $SingleSourceContainerName }))
    {   
        Write-Output "Creating new target container '$SingleSourceContainerName'."
        New-AzureStorageContainer -Name $SingleSourceContainerName -Permission Off -Context $DestStorageContext -ErrorAction Stop 
    }

    $Blobs = Get-AzureStorageBlob -Context $SourceStorageContext -Container $SingleSourceContainerName
    $BlobCpyAry = @() #Create array of objects

    #Do the copy of everything
    foreach ($Blob in $Blobs)
    {
       $BlobName = $Blob.Name
       Write-Output "Copying $BlobName from $SingleSourceContainerName"
       $BlobCopy = Start-CopyAzureStorageBlob -Context $SourceStorageContext -SrcContainer $SingleSourceContainerName -SrcBlob $BlobName -DestContext $DestStorageContext -DestContainer $SingleSourceContainerName -DestBlob $BlobName -Force
       $BlobCpyAry += $BlobCopy
    }

    #Check Status
    foreach ($BlobCopy in $BlobCpyAry)
    {
      
       $CopyState = $BlobCopy | Get-AzureStorageBlobCopyState
       $Message = $CopyState.Source.AbsolutePath + " " + $CopyState.Status + " {0:N2}%" -f (($CopyState.BytesCopied/$CopyState.TotalBytes)*100) 
       Write-Output $Message
    }
}

