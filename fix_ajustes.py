c = open("D:/CLAUDE/PROGRAMA/frontend/entrenador/ajustes.html", encoding="utf-8").read()

# Fix .main-content: remove max-width, add dark bg
c = c.replace(
    ".main-content { padding:2.5rem; overflow-y:auto; max-width:700px; }",
    ".main-content { padding:2.5rem; overflow-y:auto; background:#0d1f15; }"
)

# Add max-width wrapper to main content children via CSS instead
c = c.replace(
    ".dashboard-layout { display:grid; grid-template-columns:240px 1fr; min-height:calc(100vh - 70px); }",
    ".dashboard-layout { display:grid; grid-template-columns:240px 1fr; min-height:calc(100vh - 70px); background:#0d1f15; }"
)

# Give the inner content area a max-width via the form
c = c.replace(
    '<form id="form" style="display:none;" onsubmit="guardar(event)">',
    '<form id="form" style="display:none;max-width:700px;" onsubmit="guardar(event)">'
)

# Also limit the title area
c = c.replace(
    '<div style="margin-bottom:2rem;">',
    '<div style="margin-bottom:2rem;max-width:700px;">'
)

with open("D:/CLAUDE/PROGRAMA/frontend/entrenador/ajustes.html", "w", encoding="utf-8") as f:
    f.write(c)
print("OK")
