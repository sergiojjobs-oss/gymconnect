import { neon } from '@neondatabase/serverless';

export default async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  try {
    const sql = neon(process.env.DATABASE_URL);
    const rows = await sql`
      SELECT
        (SELECT COUNT(*) FROM entrenadores)                          AS entrenadores,
        (SELECT COUNT(*) FROM usuarios WHERE rol = 'CLIENTE')        AS clientes,
        (SELECT ROUND(AVG(rating)::numeric, 1) FROM entrenadores)    AS rating_medio
    `;
    const r = rows[0];
    res.json({
      entrenadores:   Number(r.entrenadores),
      clientesActivos: Number(r.clientes),
      valoracionMedia: Number(r.rating_medio) || 5.0
    });
  } catch (e) {
    res.status(500).json({ error: e.message });
  }
}
