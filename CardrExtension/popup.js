function onResponse(response) {
  console.log(`Received ${response}`);
}

function onError(error) {
  console.log(`Error: ${error}`);
}

function openOCRTool() {
  chrome.runtime.sendMessage("openOCRTool");  
}

function openCardrFromBtn() {
  chrome.runtime.sendMessage("openCardrFromBtn");
}

document.addEventListener('DOMContentLoaded', function() {
  var open = document.getElementById('openCardr');
  open.addEventListener('click', function() {
    if (open.disabled)
      return;
    open.innerHTML = "Launching Cardr...";
    open.disabled = true;
    open.style.backgroundColor = '#1F85DE';
    open.style.color = '#fff';
    openCardrFromBtn();
  });

  var ocr = document.getElementById('useOCR');
  ocr.addEventListener('click', function() {
    if (ocr.disabled)
      return;
    ocr.innerHTML = "Launching Cardr...";
    ocr.disabled = true;
    ocr.style.backgroundColor = '#1F85DE';
    ocr.style.color = '#fff';
    openOCRTool();
  });

  var openLink = document.getElementById('openWebsite');
  openLink.addEventListener('click', function() {
    chrome.runtime.sendMessage("openWebsite");
  });

  chrome.runtime.onMessage.addListener(function(request, sender, sendResponse) {
    if (request == "closeCardrPopup") {
      window.setTimeout(
        function() { window.close(); },
        5000
      );
    }
  });
});
