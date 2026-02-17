<!DOCTYPE html>
<html lang="en" class="theme-dark">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Store • Cambrian Vault</title>
  <link rel="stylesheet" href="assets/CV.css">
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Playfair+Display&family=Inter&display=swap" rel="stylesheet">
</head>
<body>

  <header class="site-header">
    <div class="container header-flex">
      <a href="index.php" class="logo">
        <img src="assets/images/cambrianlogo.png" alt="Cambrian Vault" class="logo-img" height="44">
      </a>
      <nav class="main-nav">
        <ul>
          <li><a href="index.php">Home</a></li>
          <li><a href="store.php" class="active">Store</a></li>
          <li><a href="about.php">About</a></li>
                 <li><a href="profile.php">Profile</a></li>
        </ul>
      </nav>
    </div>
  </header>

  <main class="store-main">
    <!-- Vault Entry Welcome -->
    <section class="store-hero">
      <div class="hero-layer">
        <img src="assets/games/gow/gow5.jpg" alt="Vault depths" class="hero-bg-img">
        <div class="hero-vignette deeper"></div>
      </div>
      <div class="hero-content container">
        <h1>The Vault Opens</h1>
        <p>Descend into the Cambrian abyss.<br>Discover, collect, evolve.</p>
      </div>
    </section>

    <!-- Top Rated Showcase – one card per game -->
    <section class="top-rated container">
      <h2>Highest Regarded Relics</h2>
      <div class="top-grid">
        <article class="relic big">
          <a href="game.php?id=gow" class="relic-link">
            <div class="relic-visual">
              <img src="assets/games/gow/gow1.jpg" alt="God of War">
            </div>
            <div class="relic-info-overlay">
              <h3>God of War</h3>
              <span>98% Positive • Mythic Action</span>
            </div>
          </a>
        </article>

        <article class="relic big">
          <a href="game.php?id=arc" class="relic-link">
            <div class="relic-visual">
              <img src="assets/games/arc/arc1.jpg" alt="Arc Raiders">
            </div>
            <div class="relic-info-overlay">
              <h3>Arc Raiders</h3>
              <span>Highly Anticipated • Extraction Shooter</span>
            </div>
          </a>
        </article>
      </div>
    </section>

    <!-- Catalog with Filters – one item per game -->
    <section class="catalog container">
      <aside class="filters">
        <h3>Filter the Depths</h3>
        <div class="filter-group">
          <strong>Genre</strong>
          <ul>
            <li>Action</li>
            <li>Adventure</li>
            <li>Roguelike</li>
            <li>Platformer</li>
            <li>Exploration</li>
          </ul>
        </div>
        <div class="filter-group">
          <strong>Era</strong>
          <ul>
            <li>Pre-2010 Classics</li>
            <li>2010s Renaissance</li>
            <li>Modern Evolutions</li>
          </ul>
        </div>
        <div class="filter-group">
          <strong>Price</strong>
          <ul>
            <li>Free</li>
            <li>Under $20</li>
            <li>$20 – $40</li>
            <li>$40+</li>
          </ul>
        </div>
      </aside>

      <div class="catalog-grid">
        <a href="game.php?id=gow" class="relic catalog-item">
          <img src="assets/games/gow/gow4.jpg" alt="God of War">
          <div class="info">
            <h4>God of War</h4>
            <span>$49.99</span>
          </div>
        </a>

        <a href="game.php?id=arc" class="relic catalog-item">
          <img src="assets/games/arc/arc3.jpg" alt="Arc Raiders">
          <div class="info">
            <h4>Arc Raiders</h4>
            <span>$39.99</span>
          </div>
        </a>
      </div>
    </section>
  </main>

  <footer class="site-footer">
    <div class="container">
      <p>© <?php echo date('Y'); ?> Cambrian Vault — Where digital seas explode with life.</p>
    </div>
  </footer>

  <script src="assets/CV.js"></script>
</body>
</html>