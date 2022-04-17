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
        showAlert("No cardr installation detected. Please visit https://cardrdebate.com/download.html to download the desktop client and begin researching! It'll only take a minute - we promise.", 'https://cardrdebate.com/download.html');
      } else {
        console.log("Received response " + JSON.stringify(response))
        chrome.runtime.sendMessage("closeCardrPopup");
      }
    }
  )
}

function getSelection() {
  return document.getSelection().toString();
}

function getOuterHTML() {
  return document.all[0].outerHTML;
}

function openCardrFromBtn() {
  chrome.tabs.query({active: true, lastFocusedWindow: true}, function(tabs) {
    var tab = tabs[0];
    if (tab.url.startsWith("chrome://")) {
      showAlert("Sorry, cardr doesn't have permissions to access chrome:// urls. Try another page.", undefined);
      return;
    }
    chrome.scripting.executeScript( {
      target: {tabId: tab.id},
      func: getSelection
    }, function(selection) {
      var selectedText = '';
      if (selection != undefined) {
        selectedText = selection[0]['result'];
      }
 
      chrome.scripting.executeScript({target: {tabId: tab.id}, func: getOuterHTML}, function(dom) {
        var html = dom[0]['result'];
        if (tab != undefined) {
          if (html.length >= 1024 * 1024 - tab.url.length - 20) { // 1 MB
            html = "<p>CARDR ERROR - This webpage was too large for cardr's Chrome extension to process. Please try again by selecting 'GO' above.</p>"
          }
        }
        openCardr(tab.url, selectedText, html)
      });
    });
  })
}

function displayAlertText(text, newURL) {
  window.alert(text);
  if (newURL != undefined) {
    window.open(newURL, '_blank')
  }
}

function showAlert(text, newURL) {
  chrome.tabs.query({active: true, lastFocusedWindow: true}, function(tabs) {
    var tab = tabs[0];
    if (tab != undefined) {
      chrome.scripting.executeScript({
        target: {tabId: tab.id},
        func: displayAlertText,
        args: [text, newURL]
      });  
    }
  });
}

function openOCRTool() {
  chrome.runtime.sendNativeMessage(
    "me.sohamgovande.cardr",
    { 
      'url': 'ocr'
    },
    function(response) {
      if (response == undefined) {
        chrome.tabs.create({ 'url': "https://cardrdebate.com/download.html" });
        showAlert("No cardr installation detected. Please visit https://cardrdebate.com/download.html to download the desktop client and begin researching! It'll only take a minute  - we promise.", 'https://cardrdebate.com/download.html');
      } else {
        console.log("Received response " + JSON.stringify(response));
        chrome.runtime.sendMessage("closeCardrPopup");
      }
    }
  )
}

chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
  console.log("Request received: " + request);
  if (request == "openCardrFromBtn") {
    openCardrFromBtn();
  } else if (request == "openOCRTool") {
    openOCRTool();
  } else if (request == "openWebsite") {
    chrome.tabs.create({ 'url': "https://cardrdebate.com" });
  }
});
