function onResponse(response) {
  console.log(`Received ${response}`);
}

function onError(error) {
  console.log(`Error: ${error}`);
}

function openCardify(urlStr, selectedText, html) {
  console.log("Detected selected text: \"" + selectedText + "\"")
  console.log("Detected DOM: "  + html)

  chrome.runtime.sendNativeMessage(
    "me.matrix4f.cardify",
    { 
      'url': urlStr, 
      'selection': selectedText,
      'html': html
    },
    function(response) {
      if (response == undefined) {
        alert("No Cardify installation detected. Please visit http://cardifydebate.x10.bz/get-started.html to download the Cardify desktop version! It'll only take a minute :-)");
        chrome.tabs.create({ 'url': "http://cardifydebate.x10.bz/download.html" });
      } else {
        console.log("Received response " + JSON.stringify(response))
      }
    }
  )
}

chrome.browserAction.onClicked.addListener(function(tab) {
  chrome.tabs.executeScript( {
    code: "window.getSelection().toString();"
  }, function(selection) {
    var selectedText = selection[0];

    chrome.tabs.executeScript({code: "document.all[0].outerHTML;"}, function(dom) {
      var html = dom[0];
      if (html.length >= 1024 * 1024 - tab.url.length - 20) { // 1 MB
        html = "<p>CARDIFY ERROR - This webpage was too large for Cardify to process. Please try again by selecting 'GO' above.</p>"
      }
      openCardify(tab.url, selectedText, html)
    });
  });
});
