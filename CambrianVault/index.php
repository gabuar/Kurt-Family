<!DOCTYPE html>
<html lang="en" class="theme-dark">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Cambrian Vault • Explosion of Digital Life</title>
  <link rel="stylesheet" href="assets/CV.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:ital,wght@0,400..900;1,400..900&family=Inter:wght@100..900&display=swap" rel="stylesheet">
</head>
<body>

  <!-- Header -->
  <header class="site-header">
    <div class="container header-flex">
      <a href="index.php" class="logo">
        <img src="assets/images/cambrianlogo.png" alt="Cambrian Vault" class="logo-img" height="44">
      </a>
      <nav class="main-nav">
        <ul>
          <li><a href="about.php">About</a></li>
        </ul>
      </nav>
    </div>
  </header>

  <main>
    <!-- Hero -->
    <section class="hero">
      <div class="hero-layer">
        <video class="hero-video" autoplay muted loop playsinline poster="assets/games/gow/gow1.jpg">
          <source src="assets/games/gow/gowvid.mp4" type="video/mp4">
        </video>
        <canvas id="life-canvas" class="life-particles"></canvas>
        <div class="hero-vignette"></div>
      </div>

      <div class="hero-content container">
        <h1 class="vault-title">Cambrian Vault</h1>
        <p class="vault-subtitle">Ancient digital seas. Sudden explosion of life.<br>Curated relics from forgotten depths await.</p>
        <div class="hero-actions">
          <a href="store.php" class="btn btn-primary">Enter the Vault</a>
          <a href="#relics" class="btn btn-outline">See First Forms</a>
        </div>
      </div>
    </section>

    <!-- Featured / Top Rated Relics – ONE CARD PER GAME -->
    <section id="relics" class="relics-section">
      <div class="container">
        <h2 class="section-title">Top Rated Relics</h2>
        <p class="section-lead">The most revered forms from the Cambrian burst.</p>

        <div class="relics-grid">

          <!-- God of War (single game) -->
          <article class="relic">
            <a href="game.php?id=gow" class="relic-link">
              <div class="relic-visual">
                <img src="assets/games/gow/gow1.jpg" alt="God of War" loading="lazy">
                <div class="visual-glow"></div>
              </div>
              <div class="relic-body">
                <h3>God of War</h3>
                <p class="relic-type">Mythic Action Adventure • 98% Positive</p>
                <div class="relic-foot">
                  <span class="price">$49.99</span>
                  <span class="btn btn-small">Unseal</span>
                </div>
              </div>
            </a>
          </article>

          <!-- Arc Raiders (single game) -->
          <article class="relic">
            <a href="game.php?id=arc" class="relic-link">
              <div class="relic-visual">
                <img src="assets/games/arc/arc1.jpg" alt="Arc Raiders" loading="lazy">
                <div class="visual-glow"></div>
              </div>
              <div class="relic-body">
                <h3>Arc Raiders</h3>
                <p class="relic-type">Extraction Shooter • Highly Anticipated</p>
                <div class="relic-foot">
                  <span class="price">$39.99</span>
                  <span class="btn btn-small">Unseal</span>
                </div>
              </div>
            </a>
          </article>

        </div>
      </div>
    </section>
  </main>

  <footer class="site-footer">
    <div class="container">
      <p>© <?php echo date('Y'); ?> Cambrian Vault — Deep digital oceans.</p>
    </div>
  </footer>

  <script src="assets/CV.js"></script>
</body>
</html>