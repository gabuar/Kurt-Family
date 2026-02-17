<!DOCTYPE html>
<html lang="en" class="theme-dark">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Profile • Cambrian Vault</title>
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
          <li><a href="profile.php" class="active">Profile</a></li>
        </ul>
      </nav>
    </div>
  </header>

  <main class="profile-page">

    <!-- Profile Hero -->
    <section class="profile-hero">
      <div class="hero-layer">
        <div class="profile-bg-gradient"></div>
        <div class="hero-vignette"></div>
      </div>

      <div class="hero-content container">
        <div class="profile-header-wrapper">
          <div class="profile-header">
            <div class="avatar-frame">
              <img src="assets/images/default-avatar.jpg" alt="User Avatar" class="avatar-img">
              <div class="level-badge">LVL 42<br>Vault Diver</div>
            </div>

            <div class="profile-info">
              <h1 class="profile-name">Deep Diver Voully</h1>
              <p class="profile-meta">
                Makati City, PH • Joined Feb 2024 • 1,243 hrs in Vault
              </p>
              <p class="profile-bio">
                Exploring ancient digital seas. Curator of forgotten relics.<br>
                "In the depths, true forms emerge."
              </p>
              <div class="profile-actions">
                <!-- Scrolls down to #library instead of navigating away -->
                <a href="#library" class="btn btn-primary" id="view-library-btn">View Library</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <div class="container">

      <!-- Stats Overview -->
      <section class="stats-overview">
        <h2 class="section-title">Vault Stats</h2>
        <div class="stats-grid">
          <div class="stat-card">
            <div class="stat-number">127</div>
            <div class="stat-label">Relics Owned</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">1,243</div>
            <div class="stat-label">Hours Explored</div>
          </div>
          <div class="stat-card">
            <div class="stat-number">42</div>
            <div class="stat-label">Allied Divers</div>
          </div>
        </div>
      </section>

      <!-- Recently Played -->
      <section class="owned-relics">
        <h2 class="section-title">Recently Played</h2>
        <p class="section-lead">Your most recent dives into the vault.</p>
        <div class="relics-grid">
          <article class="relic owned-relic">
            <a href="game.php?id=gow" class="relic-link">
              <div class="relic-visual">
                <img src="assets/games/gow/gow1.jpg" alt="God of War" loading="lazy">
                <div class="visual-glow"></div>
              </div>
              <div class="relic-body">
                <h3>God of War</h3>
                <p class="relic-type">Mythic Action Adventure • 98% Positive</p>
                <div class="relic-hours">156 hrs</div>
                <div class="relic-foot">
                  <span class="btn btn-small">Dive In</span>
                </div>
              </div>
            </a>
          </article>

          <article class="relic owned-relic">
            <a href="game.php?id=arc" class="relic-link">
              <div class="relic-visual">
                <img src="assets/games/arc/arc1.jpg" alt="Arc Raiders" loading="lazy">
                <div class="visual-glow"></div>
              </div>
              <div class="relic-body">
                <h3>Arc Raiders</h3>
                <p class="relic-type">Extraction Shooter • Highly Anticipated</p>
                <div class="relic-hours">23 hrs</div>
                <div class="relic-foot">
                  <span class="btn btn-small">Dive In</span>
                </div>
              </div>
            </a>
          </article>
        </div>
      </section>

      <!-- ══════════════════════════════════════════
           FULL LIBRARY — anchor target
      ══════════════════════════════════════════ -->
      <section class="library-section" id="library">

        <h2 class="section-title">
          Full Library
          <span class="library-count" id="lib-count">127</span>
        </h2>
        <p class="section-lead">Every relic in your vault.</p>

        <!-- Search + Filters -->
        <div class="library-controls">
          <input
            type="text"
            class="library-search"
            id="lib-search"
            placeholder="Search your vault…"
            autocomplete="off"
          >
          <div class="filter-tabs">
            <button class="filter-tab active" data-filter="all">All</button>
            <button class="filter-tab" data-filter="action">Action</button>
            <button class="filter-tab" data-filter="rpg">RPG</button>
            <button class="filter-tab" data-filter="shooter">Shooter</button>
            <button class="filter-tab" data-filter="strategy">Strategy</button>
            <button class="filter-tab" data-filter="indie">Indie</button>
          </div>
        </div>

        <!-- Library Grid -->
        <div class="library-grid" id="lib-grid">

          <!-- Cards are rendered by JS below; placeholder data is defined in the script -->

        </div>

        <p class="library-empty" id="lib-empty">No relics match your search.</p>

      </section>

      <!-- Friends -->
      <section class="friends-section">
        <h2 class="section-title">Allied Divers</h2>
        <div class="friends-grid">
          <a href="#" class="friend-card">
            <img src="assets/images/friend1.jpg" alt="Friend 1" class="friend-avatar">
            <span class="friend-name">Echo Diver</span>
            <span class="friend-status online">Online</span>
          </a>
          <a href="#" class="friend-card">
            <img src="assets/images/friend2.jpg" alt="Friend 2" class="friend-avatar">
            <span class="friend-name">Abyss Scout</span>
            <span class="friend-status offline">Offline</span>
          </a>
        </div>
      </section>

    </div><!-- /.container -->
  </main>

  <footer class="site-footer">
    <div class="container">
      <p>© <?php echo date('Y'); ?> Cambrian Vault — Deep digital oceans.</p>
    </div>
  </footer>

  <script src="assets/CV.js"></script>
  <script>
    /* ── Library data ──────────────────────────────────────────
       Replace image paths with your real assets.
       genre values must match the filter-tab data-filter attrs.
    ──────────────────────────────────────────────────────────── */
    const LIBRARY = [
      { id: 'gow', title: 'God of War',  type: 'Mythic Action Adventure', genre: 'action',  hours: 156, img: 'assets/games/gow/gow1.jpg', emoji: '⚔️' },
      { id: 'arc', title: 'Arc Raiders', type: 'Extraction Shooter',       genre: 'shooter', hours: 23,  img: 'assets/games/arc/arc1.jpg', emoji: '🔫' },
    ];

    /* Gradient palettes for placeholder thumbnails */
    const THUMB_GRADIENTS = [
      'linear-gradient(135deg, #0d1b3e 0%, #00344d 100%)',
      'linear-gradient(135deg, #1a0d3e 0%, #3d0050 100%)',
      'linear-gradient(135deg, #0d2e1a 0%, #003d30 100%)',
      'linear-gradient(135deg, #3e1a0d 0%, #4d2000 100%)',
      'linear-gradient(135deg, #1a1a0d 0%, #2d3d00 100%)',
      'linear-gradient(135deg, #0d1e3e 0%, #001a4d 100%)',
    ];

    function gradientForId(id) {
      let hash = 0;
      for (const c of id) hash = (hash * 31 + c.charCodeAt(0)) & 0xffff;
      return THUMB_GRADIENTS[hash % THUMB_GRADIENTS.length];
    }

    function fmtHours(n) {
      return n.toLocaleString() + ' hrs';
    }

    function buildCard(game) {
      const card = document.createElement('a');
      card.href = `game.php?id=${game.id}`;
      card.className = 'lib-card';
      card.dataset.genre = game.genre;
      card.dataset.title = game.title.toLowerCase();

      const thumbInner = game.img
        ? `<img src="${game.img}" alt="${game.title}" loading="lazy">`
        : `<div class="lib-thumb-placeholder" style="background:${gradientForId(game.id)}">${game.emoji}</div>`;

      card.innerHTML = `
        <div class="lib-card-thumb">
          ${thumbInner}
          <div class="lib-card-thumb-overlay"></div>
          <span class="lib-dive-btn">Dive In</span>
        </div>
        <div class="lib-card-body">
          <span class="lib-card-title">${game.title}</span>
          <span class="lib-card-type">${game.type}</span>
          <span class="lib-card-hours">${fmtHours(game.hours)}</span>
        </div>
      `;
      return card;
    }

    /* ── Render ────────────────────────────────────────────── */
    const grid     = document.getElementById('lib-grid');
    const empty    = document.getElementById('lib-empty');
    const countEl  = document.getElementById('lib-count');
    const searchEl = document.getElementById('lib-search');

    let activeFilter = 'all';

    function render() {
      const q = searchEl.value.trim().toLowerCase();
      const cards = LIBRARY.filter(g => {
        const matchGenre = activeFilter === 'all' || g.genre === activeFilter;
        const matchQuery = !q || g.title.toLowerCase().includes(q) || g.type.toLowerCase().includes(q);
        return matchGenre && matchQuery;
      });

      grid.innerHTML = '';
      cards.forEach(g => grid.appendChild(buildCard(g)));
      empty.style.display = cards.length === 0 ? 'block' : 'none';
      countEl.textContent = cards.length;
    }

    render();

    /* ── Filter tabs ───────────────────────────────────────── */
    document.querySelectorAll('.filter-tab').forEach(tab => {
      tab.addEventListener('click', () => {
        document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
        tab.classList.add('active');
        activeFilter = tab.dataset.filter;
        render();
      });
    });

    /* ── Search ────────────────────────────────────────────── */
    searchEl.addEventListener('input', render);

    /* ── Smooth scroll for "View Library" btn ──────────────── */
    document.getElementById('view-library-btn').addEventListener('click', e => {
      e.preventDefault();
      document.getElementById('library').scrollIntoView({ behavior: 'smooth', block: 'start' });
    });

    /* ── Legacy scripts from original ─────────────────────── */
    // hours comma formatting for recently played
    document.querySelectorAll('.relic-hours').forEach(el => {
      let hrs = el.textContent.match(/\d+/)[0];
      el.textContent = hrs.replace(/\B(?=(\d{3})+(?!\d))/g, ',') + ' hrs';
    });

    // online glow
    document.querySelectorAll('.friend-status.online').forEach(el => {
      el.style.color = 'var(--accent)';
      el.style.textShadow = '0 0 8px var(--accent-glow)';
    });

    // owned relic hover
    document.querySelectorAll('.owned-relic').forEach(card => {
      card.addEventListener('mouseenter', () => {
        card.style.boxShadow = '0 0 55px rgba(0,234,255,0.5), 0 25px 70px rgba(0,0,0,0.6)';
        card.style.transform = 'translateY(-16px) scale(1.035)';
        const btn = card.querySelector('.btn-small');
        if (btn) btn.style.boxShadow = '0 0 20px rgba(0,255,170,0.6)';
      });
      card.addEventListener('mouseleave', () => {
        card.style.boxShadow = '';
        card.style.transform = '';
        const btn = card.querySelector('.btn-small');
        if (btn) btn.style.boxShadow = '';
      });
    });
  </script>
</body>
</html>