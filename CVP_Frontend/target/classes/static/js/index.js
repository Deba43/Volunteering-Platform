// index.js

document.addEventListener('DOMContentLoaded', function () {
  // Initialize Particles.js with desired configuration
  particlesJS("particles-js", {
    "particles": {
      "number": { "value": 80, "density": { "enable": true, "value_area": 800 }},
      "color": { "value": "#ffffff" },
      "shape": { "type": "circle" },
      "opacity": { "value": 0.5, "random": false },
      "size": { "value": 3, "random": true },
      "line_linked": { "enable": true, "distance": 150, "color": "#ffffff", "opacity": 0.4, "width": 1 },
      "move": { "enable": true, "speed": 3, "direction": "none" }
    },
    "interactivity": {
      "events": { "onhover": { "enable": true, "mode": "repulse" }},
      "modes": { "repulse": { "distance": 100, "duration": 0.4 }}
    },
    "retina_detect": true
  });
});
