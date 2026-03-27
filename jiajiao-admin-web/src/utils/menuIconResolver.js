import {
  BookOpen,
  ChartNoAxesCombined,
  Circle,
  ClipboardList,
  FolderKanban,
  GraduationCap,
  LayoutDashboard,
  MapPinned,
  MonitorCog,
  ShieldCheck,
  SlidersHorizontal,
  SquareMenu,
  Users
} from 'lucide-vue-next'

const icons = {
  BookOpen,
  ChartNoAxesCombined,
  Circle,
  ClipboardList,
  FolderKanban,
  GraduationCap,
  LayoutDashboard,
  MapPinned,
  MonitorCog,
  ShieldCheck,
  SlidersHorizontal,
  SquareMenu,
  Users
}

const aliases = {
  dashboard: 'LayoutDashboard',
  user: 'Users',
  users: 'Users',
  teacher: 'GraduationCap',
  audit: 'ShieldCheck',
  requirement: 'ClipboardList',
  order: 'FolderKanban',
  subject: 'BookOpen',
  school: 'GraduationCap',
  region: 'MapPinned',
  role: 'ShieldCheck',
  menu: 'SquareMenu',
  dictionary: 'SlidersHorizontal',
  slide: 'MonitorCog',
  advertising: 'MonitorCog',
  stats: 'ChartNoAxesCombined'
}

function toPascalCase(text) {
  return String(text)
    .trim()
    .replace(/^el-icon-/i, '')
    .replace(/[^\w\s-]/g, '')
    .split(/[\s_-]+/)
    .filter(Boolean)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join('')
}

export function resolveMenuIcon(name, fallback = 'Circle') {
  if (!name) {
    return icons[fallback] || Circle
  }

  const direct = icons[name]
  if (direct) return direct

  const pascal = toPascalCase(name)
  if (icons[pascal]) return icons[pascal]

  const alias = aliases[String(name).trim().toLowerCase()]
  if (alias && icons[alias]) return icons[alias]

  return icons[fallback] || Circle
}

