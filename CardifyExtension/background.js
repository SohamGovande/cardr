function onResponse(response) {
  console.log(`Received ${response}`);
}

function onError(error) {
  console.log(`Error: ${error}`);
}

chrome.browserAction.onClicked.addListener(function(tab) {
  chrome.tabs.executeScript( {
    code: "window.getSelection().toString();"
  }, function(selection) {
    var selectedText = selection[0];

    chrome.tabs.executeScript({code: "document.all[0].outerHTML;"}, function(dom) {
      var html = dom[0];
      console.log("Detected selected text: \"" + selectedText + "\"")
      console.log("Detected DOM: "  + html)

      chrome.runtime.sendNativeMessage(
        "me.matrix4f.cardify",
        { 
          'url': tab.url, 
          'selection': selectedText,
          'html': html
        },
        function(response) {
          if (response == undefined) {
            alert("No Cardify installation detected. Please visit http://cardifydebate.x10.bz/get-started.html to download the Cardify desktop version! It'll only take a minute :-)");
            chrome.tabs.create({ url: "http://cardifydebate.x10.bz/download.html" });
          } else {
            console.log("Received response " + JSON.stringify(response))
          }
        }
      );
    });
  });
});
