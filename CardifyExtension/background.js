function onResponse(response) {
  console.log(`Received ${response}`);
}

function onError(error) {
  console.log(`Error: ${error}`);
}

chrome.browserAction.onClicked.addListener(function(tab) {  
  chrome.runtime.sendNativeMessage(
    "me.matrix4f.cardify",
    { 'url': tab.url },
    function(response) {
      if (response == undefined) {
        alert("You haven't yet installed the Cardify Native Client. Please visit http://cardifydebate.x10.bz/get-started.html to get started with Cardify! We promise it won't take you more than 5 minutes.");
      } else {
        console.log("Received response " + JSON.stringify(response))
      }
    }
  );
});
