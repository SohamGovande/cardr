function onResponse(response) {
  console.log(`Received ${response}`);
}

function onError(error) {
  console.log(`Error: ${error}`);
}

function openCardr(urlStr, selectedText, html) {
  console.log("Detected selected text: \"" + selectedText + "\"")
  console.log("Detected DOM: "  + html)

  chrome.runtime.sendNativeMessage(
    "me.sohamgovande.cardr",
    { 
      'url': urlStr, 
      'selection': selectedText,
      'html': html
    },
    function(response) {
      if (response == undefined) {
        alert("No cardr installation detected. Please visit http://cardr.x10.bz/get-started.html to download the desktop client and begin researching! It'll only take a minute :) Click 'OK' to download it.");
        chrome.tabs.create({ 'url': "http://cardr.x10.bz/get-started.html" });
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
    var selectedText = '';
    if (selection != undefined) {
      selectedText = selection[0];
    }

    chrome.tabs.executeScript({code: "document.all[0].outerHTML;"}, function(dom) {
      var html = dom[0];
      if (html.length >= 1024 * 1024 - tab.url.length - 20) { // 1 MB
        html = "<p>CARDR ERROR - This webpage was too large for cardr's Chrome extension to process. Please try again by selecting 'GO' above.</p>"
      }
      openCardr(tab.url, selectedText, html)
    });
  });
});
