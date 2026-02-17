// CV.js - Aquatic bioluminescent interactions (cursor removed, hover & particles only)

// Life particles - more aquatic/plankton feel
const canvas = document.getElementById('life-canvas');
if (canvas) {
  const ctx = canvas.getContext('2d');
  let particles = [];
  let mouse = { x: null, y: null, radius: 180 };

  function resizeCanvas() {
    canvas.width = canvas.parentElement.offsetWidth;
    canvas.height = canvas.parentElement.offsetHeight;
  }
  window.addEventListener('resize', resizeCanvas);
  resizeCanvas();

  window.addEventListener('mousemove', e => {
    const rect = canvas.getBoundingClientRect();
    mouse.x = e.clientX - rect.left;
    mouse.y = e.clientY - rect.top;
  });
  window.addEventListener('mouseout', () => { mouse.x = mouse.y = null; });

  class Particle {
    constructor() {
      this.x = Math.random() * canvas.width;
      this.y = Math.random() * canvas.height;
      this.baseSize = Math.random() * 6 + 2.5;
      this.size = this.baseSize;
      this.speedX = Math.random() * 0.4 - 0.2;
      this.speedY = Math.random() * 0.4 - 0.2;
      this.hue = 180 + Math.random() * 60;
      this.alpha = Math.random() * 0.4 + 0.35;
      this.pulse = Math.random() * Math.PI * 2;
    }
    update() {
      this.x += this.speedX;
      this.y += this.speedY;
      if (this.x < 0 || this.x > canvas.width) this.speedX *= -0.7;
      if (this.y < 0 || this.y > canvas.height) this.speedY *= -0.7;

      this.pulse += 0.012;
      this.size = this.baseSize + Math.sin(this.pulse) * 2.2;

      if (mouse.x && mouse.y) {
        const dx = mouse.x - this.x;
        const dy = mouse.y - this.y;
        const dist = Math.hypot(dx, dy);
        if (dist < mouse.radius) {
          const force = (mouse.radius - dist) / mouse.radius;
          this.x -= dx * force * 0.09;
          this.y -= dy * force * 0.09;
          this.alpha = Math.min(0.9, this.alpha + force * 0.3);
        }
      }
    }
    draw() {
      ctx.save();
      ctx.globalAlpha = this.alpha;
      ctx.beginPath();
      ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
      ctx.fillStyle = `hsla(${this.hue}, 90%, 75%, ${this.alpha})`;
      ctx.shadowBlur = 30;
      ctx.shadowColor = '#00ffff';
      ctx.fill();
      ctx.restore();
    }
  }

  function initParticles() {
    particles = [];
    const count = Math.floor(canvas.width * canvas.height / 14000);
    for (let i = 0; i < count; i++) particles.push(new Particle());
  }
  initParticles();
  window.addEventListener('resize', initParticles);

  function animate() {
    ctx.clearRect(0, 0, canvas.width, canvas.height);
    particles.forEach(p => { p.update(); p.draw(); });
    requestAnimationFrame(animate);
  }
  animate();
}

// Smooth scroll for anchors (kept)
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
  anchor.addEventListener('click', function(e) {
    e.preventDefault();
    document.querySelector(this.getAttribute('href')).scrollIntoView({ behavior: 'smooth' });
  });
});

// Card hover glow boost (kept – your .relic cards still get nice hover effect)
document.querySelectorAll('.relic').forEach(card => {
  card.addEventListener('mouseenter', () => {
    card.style.boxShadow = '0 0 55px rgba(0,234,255,0.5), 0 25px 70px rgba(0,0,0,0.6)';
    card.style.transform = 'translateY(-16px) scale(1.035)';
  });
  card.addEventListener('mouseleave', () => {
    card.style.boxShadow = '';
    card.style.transform = '';
  });
});