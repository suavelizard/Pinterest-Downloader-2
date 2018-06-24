var content = '.pinWrapper { display: none !important; }';
var style = document.createElement('style');
style.type = 'text/css';
style.appendChild(document.createTextNode(content));
document.head.appendChild(style);