module.exports = async function handler(req, res) {
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'POST, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') return res.status(200).end();
  if (req.method !== 'POST') return res.status(405).json({ error: 'Method not allowed' });

  const key = process.env.IMGBB_API_KEY;
  if (!key) return res.status(500).json({ error: 'Upload not configured' });

  const { image } = req.body || {};
  if (!image || typeof image !== 'string') {
    return res.status(400).json({ error: 'Missing image (base64)' });
  }

  try {
    const body = new URLSearchParams();
    body.append('key', key);
    body.append('image', image);

    const r = await fetch('https://api.imgbb.com/1/upload', {
      method: 'POST',
      body
    });

    const data = await r.json();
    if (!r.ok || !data?.data?.url) {
      return res.status(502).json({ error: 'ImgBB upload failed' });
    }

    res.json({ url: data.data.url });
  } catch {
    res.status(502).json({ error: 'Upload failed' });
  }
};
