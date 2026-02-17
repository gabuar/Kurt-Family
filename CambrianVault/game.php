<?php
$id = $_GET['id'] ?? 'gow';

$games = [
  'gow' => [
    'title'       => 'God of War',
    'subtitle'    => 'A brutal saga carved from myth and silicon.',
    'price'       => '$49.99',
    'discount'    => '-20%',
    'final_price' => '$39.99',
    'release_date'=> 'April 20, 2022',
    'developer'   => 'Santa Monica Studio',
    'publisher'   => 'PlayStation PC LLC',
    'genres'      => ['Action', 'Adventure', 'RPG', 'Hack and Slash'],
    'description' => 'His vengeance against the Gods of Olympus years behind him, Kratos now lives as a man in the realm of Norse Gods and monsters. It is in this harsh, unforgiving world that he must fight to survive… and teach his son to do the same.',
    'features'    => [
      'Stunning redesigned worlds',
      'Engaging narrative with deep character moments',
      'Over-the-top combat with new weapons & abilities',
      'Exploration of vast realms'
    ],
    'min_req'     => [
      'OS'          => 'Windows 10 64-bit',
      'Processor'   => 'Intel i5-2500k or AMD Ryzen 3 1200',
      'Memory'      => '8 GB RAM',
      'Graphics'    => 'NVIDIA GTX 960 (4GB) or AMD R9 290X (4GB)',
      'Storage'     => '70 GB available space'
    ],
    'rec_req'     => [
      'OS'          => 'Windows 10/11 64-bit',
      'Processor'   => 'Intel i5-6600k or AMD Ryzen 5 2400 G',
      'Memory'      => '16 GB RAM',
      'Graphics'    => 'NVIDIA GTX 1060 (6GB) or AMD RX 5700 (8GB)',
      'Storage'     => '70 GB available space'
    ],
    'rating'      => 'Very Positive',
    'rating_percent' => '94%',
    'rating_count'=> 52341,
    'reviews'     => [
      ['user' => 'KratosFan92', 'date' => 'Jan 15, 2026', 'text' => 'Masterpiece. The story hits hard, combat is satisfying, visuals insane on PC.'],
      ['user' => 'NordicGamer', 'date' => 'Feb 2, 2026', 'text' => 'Best port ever. Runs buttery smooth at 4K 120fps with DLSS. Worth every penny.']
    ],
    'video'       => '/assets/games/gow/gowvid.mp4',
    'poster'      => '/assets/games/gow/gow1.jpg',
    'screenshots' => [
      '/assets/games/gow/gow1.jpg',
      '/assets/games/gow/gow2.jpg',
      '/assets/games/gow/gow3.jpg',
      '/assets/games/gow/gow4.jpg',
      '/assets/games/gow/gow5.jpg'
    ]
  ],
  'arc' => [
    'title'       => 'Arc Raiders',
    'subtitle'    => 'High-stakes extraction in a machine-overrun world.',
    'price'       => '$39.99',
    'discount'    => '',
    'final_price' => '$39.99',
    'release_date'=> 'TBA 2026',
    'developer'   => 'Embark Studios',
    'publisher'   => 'Embark Studios',
    'genres'      => ['Shooter', 'Survival', 'Co-op', 'PvPvE'],
    'description' => 'Team up with friends to raid the surface, scavenge resources, and survive against ARC machines in this intense PvPvE extraction shooter.',
    'features'    => [
      'Intense PvPvE extraction gameplay',
      'Dynamic co-op battles',
      'Craft and customize gear',
      'Procedural environments'
    ],
    'min_req'     => [
      'OS'          => 'Windows 10 64-bit',
      'Processor'   => 'Intel i5-10400',
      'Memory'      => '16 GB RAM',
      'Graphics'    => 'NVIDIA GTX 1660 (6GB)',
      'Storage'     => '50 GB available space'
    ],
    'rec_req'     => [
      'OS'          => 'Windows 11 64-bit',
      'Processor'   => 'Intel i7-10700K',
      'Memory'      => '32 GB RAM',
      'Graphics'    => 'NVIDIA RTX 3070 (8GB)',
      'Storage'     => '50 GB available space'
    ],
    'rating'      => 'Highly Anticipated',
    'rating_percent' => 'N/A',
    'rating_count'=> 0,
    'reviews'     => [
      ['user' => 'RaiderBeta', 'date' => 'Dec 10, 2025', 'text' => 'Beta was intense! Love the extraction mechanics and machine designs.'],
      ['user' => 'CoopFan', 'date' => 'Jan 5, 2026', 'text' => 'Teamwork shines here. Can\'t wait for full release.']
    ],
    'video'       => '/assets/games/arc/arcvid.mp4',
    'poster'      => '/assets/games/arc/arc1.jpg',
    'screenshots' => [
      '/assets/games/arc/arc1.jpg',
      '/assets/games/arc/arc2.jpg',
      '/assets/games/arc/arc3.jpg',
      '/assets/games/arc/arc4.jpg',
      '/assets/games/arc/arc5.jpg'
    ]
  ]
];

$game = $games[$id] ?? $games['gow'];
$clean_count = (int) str_replace(',', '', $game['rating_count']);
?>

<!DOCTYPE html>
<html lang="en" class="theme-dark">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title><?= htmlspecialchars($game['title']) ?> • Cambrian Vault</title>
  <link rel="stylesheet" href="/assets/CV.css">
  <link href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@400;700&family=Inter:wght@300;400;500;600&display=swap" rel="stylesheet">
</head>
<body>

  <header class="site-header">
    <div class="container header-flex">
      <a href="/index.php" class="logo">
        <img src="/assets/images/cambrianlogo.png" alt="Cambrian Vault" class="logo-img" height="44">
      </a>
      <nav class="main-nav">
        <ul>
          <li><a href="/index.php">Home</a></li>
          <li><a href="/store.php">Store</a></li>
          <li><a href="/about.php">About</a></li>
        </ul>
      </nav>
    </div>
  </header>

  <main class="game-page">

    <section class="game-hero">
      <video class="hero-video" autoplay muted loop playsinline poster="<?= htmlspecialchars($game['poster']) ?>">
        <source src="<?= htmlspecialchars($game['video']) ?>" type="video/mp4">
        Your browser does not support the video tag.
      </video>
      <div class="hero-gradient"></div>
      <div class="hero-vignette"></div>

      <!-- hero-content no longer needs .container — game-hero-body handles centering -->
      <div class="hero-content">
        <div class="game-hero-body">

          <!-- LEFT: title, subtitle, genres, meta -->
          <div class="game-hero-left">
            <h1 class="game-title"><?= htmlspecialchars($game['title']) ?></h1>
            <p class="game-subtitle"><?= htmlspecialchars($game['subtitle']) ?></p>
            <div class="genres">
              <?php foreach ($game['genres'] as $genre): ?>
                <span class="genre-tag"><?= htmlspecialchars($genre) ?></span>
              <?php endforeach; ?>
            </div>
            <p class="meta">
              Developer: <?= htmlspecialchars($game['developer']) ?> •
              Publisher: <?= htmlspecialchars($game['publisher']) ?> •
              Release: <?= htmlspecialchars($game['release_date']) ?>
            </p>
          </div>

          <!-- RIGHT: price + buttons -->
          <div class="game-hero-right">
            <div class="price-actions">
              <?php if ($game['discount']): ?>
                <span class="discount"><?= $game['discount'] ?></span>
              <?php endif; ?>
              <span class="final-price"><?= $game['final_price'] ?></span>
              <?php if ($game['discount']): ?>
                <span class="original-price"><?= $game['price'] ?></span>
              <?php endif; ?>
            </div>
            <div class="action-buttons">
              <a href="#" class="btn btn-buy">Add to Cart</a>
              <a href="#" class="btn btn-wishlist">+ Wishlist</a>
            </div>
          </div>

        </div>
      </div>
    </section>

    <div class="container main-grid">
      <div class="description-column">
        <h2 class="section-title">About This Relic</h2>
        <p class="description"><?= nl2br(htmlspecialchars($game['description'])) ?></p>

        <?php if (!empty($game['features'])): ?>
          <h3 class="sub-title">Key Mutations</h3>
          <ul class="features-list">
            <?php foreach ($game['features'] as $feature): ?>
              <li><?= htmlspecialchars($feature) ?></li>
            <?php endforeach; ?>
          </ul>
        <?php endif; ?>

        <h3 class="sub-title">Gallery</h3>
        <div class="screenshots">
          <?php foreach ($game['screenshots'] as $shot): ?>
            <img src="<?= htmlspecialchars($shot) ?>" alt="Screenshot" loading="lazy">
          <?php endforeach; ?>
        </div>
      </div>

      <div class="requirements-column">
        <h2 class="section-title">System Depths</h2>
        <div class="req-cards">
          <div class="req-card min">
            <h3>Minimum</h3>
            <?php foreach ($game['min_req'] as $key => $val): ?>
              <div class="req-item">
                <strong><?= htmlspecialchars($key) ?>:</strong> <?= htmlspecialchars($val) ?>
              </div>
            <?php endforeach; ?>
          </div>
          <div class="req-card rec">
            <h3>Recommended</h3>
            <?php foreach ($game['rec_req'] as $key => $val): ?>
              <div class="req-item">
                <strong><?= htmlspecialchars($key) ?>:</strong> <?= htmlspecialchars($val) ?>
              </div>
            <?php endforeach; ?>
          </div>
        </div>
      </div>
    </div>

    <section class="reviews container">
      <h2 class="section-title">Community Echoes</h2>
      <div class="rating-overview">
        <div class="rating-circle"><?= htmlspecialchars($game['rating_percent']) ?></div>
        <div class="rating-text">
          <strong><?= htmlspecialchars($game['rating']) ?></strong><br>
          <?= number_format($game['rating_count']) ?> user reviews
        </div>
      </div>

      <div class="review-cards">
        <?php foreach ($game['reviews'] as $review): ?>
          <div class="review-card">
            <div class="review-meta">
              <strong><?= htmlspecialchars($review['user']) ?></strong>
              <span><?= htmlspecialchars($review['date']) ?></span>
            </div>
            <p><?= htmlspecialchars($review['text']) ?></p>
          </div>
        <?php endforeach; ?>
      </div>
    </section>

  </main>

  <footer class="site-footer">
    <div class="container">
      <p>© <?= date('Y') ?> Cambrian Vault – All rights reserved.</p>
    </div>
  </footer>

  <script src="/assets/CV.Js"></script>
</body>
</html>