module.exports = async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  try {
    const url = process.env.DATABASE_URL;
    // Parse connection string: postgresql://user:pass@host/db
    const match = url.match(/postgresql:\/\/([^:]+):([^@]+)@([^/]+)\/([^?]+)/);
    if (!match) throw new Error('Invalid DATABASE_URL');
    const [, user, password, host, database] = match;

    const response = await fetch(`https://${host}/sql`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Neon-Connection-String': url
      },
      body: JSON.stringify({
        query: `SELECT
          (SELECT COUNT(*) FROM entrenadores)::int AS entrenadores,
          (SELECT COUNT(*) FROM usuarios WHERE rol = 'CLIENTE')::int AS clientes,
          (SELECT ROUND(AVG(rating)::numeric, 1) FROM entrenadores)::float AS rating_medio`
      })
    });

    if (!response.ok) {
      const txt = await response.text();
      throw new Error(`Neon HTTP error: ${response.status} ${txt}`);
    }

    const data = await response.json();
    const r = data.rows[0];
    res.json({
      entrenadores:    r.entrenadores,
      clientesActivos: r.clientes,
      valoracionMedia: r.rating_medio || 5.0
    });
  } catch (e) {
    console.error('stats error:', e.message);
    res.status(500).json({ error: e.message });
  }
};
