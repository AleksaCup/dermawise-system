export interface Option<T extends string> {
  value: T
  title: string
}

function options<T extends string>(labels: Record<T, string>): Option<T>[] {
  return (Object.keys(labels) as T[]).map((value) => ({ value, title: labels[value] }))
}

export const SKIN_COLORS = options({
  VERY_FAIR: 'Vrlo svetla',
  FAIR: 'Svetla',
  LIGHT_BROWN: 'Svetlosmeđa',
  OLIVE: 'Maslinasta',
  DARK_BROWN: 'Tamnosmeđa',
  BLACK: 'Crna',
})

export const EYE_COLORS = options({
  BLUE: 'Plave',
  GREY: 'Sive',
  GREEN: 'Zelene',
  HAZEL: 'Boje lešnika',
  BROWN: 'Smeđe',
  DARK_BROWN: 'Tamnosmeđe',
})

export const HAIR_COLORS = options({
  RED: 'Riđa',
  BLONDE: 'Plava',
  LIGHT_BROWN: 'Svetlosmeđa',
  DARK_BROWN: 'Tamnosmeđa',
  BLACK: 'Crna',
})

export const ETHNIC_ORIGINS = options({
  NORTHERN_EUROPEAN: 'Severna Evropa',
  SOUTHERN_EUROPEAN: 'Južna Evropa',
  MIDDLE_EASTERN: 'Bliski istok',
  SOUTH_ASIAN: 'Južna Azija',
  EAST_ASIAN: 'Istočna Azija',
  LATIN_AMERICAN: 'Latinska Amerika',
  AFRICAN: 'Afrika',
  OTHER: 'Drugo',
})

export const SUN_REACTIONS = options({
  ALWAYS_BURNS: 'Uvek dobijem opekotine',
  BURNS_THEN_TANS: 'Prvo opekotine, zatim preplanulost',
  RARELY_BURNS: 'Retko dobijem opekotine',
  NEVER_BURNS: 'Nikad ne dobijem opekotine',
})

export const TAN_TENDENCIES = options({
  NEVER: 'Nikad ne pocrnim',
  MINIMAL: 'Slabo',
  GRADUAL: 'Postepeno',
  EASILY: 'Lako',
  DEEPLY: 'Izrazito',
})

export const FACE_REGIONS = options({
  FOREHEAD: 'Čelo',
  RIGHT_CHEEK: 'Desni obraz',
  LEFT_CHEEK: 'Levi obraz',
  NOSE: 'Nos',
  CHIN: 'Brada',
  CHEST_BACK: 'Grudi i gornji deo leđa',
})

export const LESION_TYPES = options({
  NONE: 'Nema promena',
  COMEDONES: 'Komedoni (mitiseri)',
  PAPULES: 'Papule (crvene kvržice)',
  PUSTULES: 'Pustule (sa gnojem)',
  NODULES: 'Noduli (duboki i bolni)',
})

export const BODY_AREAS = options({
  FOREHEAD: 'Čelo',
  CHEEKS: 'Obrazi',
  NOSE: 'Nos',
  CHIN: 'Brada',
  PERIORAL: 'Oko usta',
  NASOLABIAL_FOLDS: 'Nazolabijalne bore',
  EYEBROWS: 'Obrve',
  EYELIDS: 'Kapci',
  SCALP: 'Skalp',
  BEHIND_EARS: 'Iza ušiju',
  NECK: 'Vrat',
  CHEST_BACK: 'Grudi i leđa',
  HANDS: 'Šake',
  BODY: 'Telo',
})

export const GRADE_LABELS = ['Nema', 'Blago', 'Umereno', 'Izraženo']

export function label<T extends string>(list: Option<T>[], value: T | null | undefined): string {
  return list.find((o) => o.value === value)?.title ?? (value ?? '')
}

export const CONDITION_LABELS: Record<string, string> = {
  ACNE: 'Akne',
  ROSACEA: 'Rozaceja',
  PERIORAL_DERMATITIS: 'Perioralni dermatitis',
  ATOPIC_DERMATITIS: 'Atopijski dermatitis',
  SEBORRHEIC_DERMATITIS: 'Seboroični dermatitis',
  CONTACT_DERMATITIS: 'Kontaktni dermatitis',
}

export const SEVERITY_LABELS: Record<string, string> = {
  MILD: 'blag oblik',
  MODERATE: 'umeren oblik',
  SEVERE: 'težak oblik',
  VERY_SEVERE: 'vrlo težak oblik',
}

export const SUBTYPE_LABELS: Record<string, string> = {
  ETR: 'eritematoteleangiektatična (ETR)',
  PPR: 'papulopustularna (PPR)',
  PHYMATOUS: 'fimatozna',
  OCULAR: 'okularna',
}

export const STEP_LABELS: Record<string, string> = {
  CLEANSING: 'Čišćenje',
  TREATMENT: 'Tretman',
  HYDRATION: 'Hidratacija',
  SPF: 'Zaštita od sunca',
}

export const PHASE_LABELS: Record<string, string> = {
  phototype: 'Fototip',
  diagnosis: 'Dijagnostika',
  therapy: 'Terapija',
  safety: 'Bezbednost',
  MAIN: 'Dnevnik',
}

export const STATUS_INFO: Record<string, { label: string; color: string }> = {
  ACTIVE: { label: 'U rutini', color: 'success' },
  PENDING_REVIEW: { label: 'Čeka potvrdu dermatologa', color: 'warning' },
  APPROVED: { label: 'Odobrio dermatolog', color: 'success' },
  REJECTED: { label: 'Odbio dermatolog', color: 'error' },
  WITHDRAWN: { label: 'Povučeno', color: 'error' },
}

export function roman(value: number): string {
  return ['', 'I', 'II', 'III', 'IV', 'V', 'VI'][value] ?? '-'
}

export function formatDate(value: string): string {
  return new Date(value).toLocaleString('sr-Latn-RS', { dateStyle: 'medium', timeStyle: 'short' })
}

export function scoreLabel(condition: string, score: number | null): string {
  if (score === null) {
    return ''
  }
  if (condition === 'ACNE') {
    return `GAGS ${score}`
  }
  if (condition === 'ATOPIC_DERMATITIS') {
    return `SCORAD ${score}`
  }
  return String(score)
}

export const FLUSH_TRIGGERS = options({
  HRANA: 'Hrana',
  ALKOHOL: 'Alkohol',
  TOPLOTA: 'Toplota',
  STRES: 'Stres',
  KOZMETIKA: 'Kozmetika',
})

export const SYMPTOM_TYPES = options({
  ERYTHEMA: 'Crvenilo',
  STINGING: 'Peckanje',
  ITCHING: 'Svrab',
  SCALING: 'Ljuštenje',
})

export const ENTRY_TYPE_LABELS: Record<string, string> = {
  FLUSHING: 'Flushing',
  SUN: 'Sunce',
  NEW_PRODUCT: 'Novi proizvod',
  SYMPTOM: 'Simptom',
}

export const ALERT_LABELS: Record<string, string> = {
  ROSACEA_SUSPICION: 'Sumnja na rozaceju',
  DOMINANT_TRIGGER: 'Okidač flushinga',
  HIGH_UV_EXPOSURE: 'Previše sunca',
  VERY_HIGH_UV: 'Vrlo visok UV',
  PHOTOAGING_RISK: 'Rizik od fotostarenja',
  SEBORRHEIC_SYMPTOMS: 'Seboroični simptomi',
  CONTACT_REACTION: 'Moguća kontaktna reakcija',
  SAFETY_NOTE: 'Bezbednosna napomena',
}

export function nowForInput(): string {
  const now = new Date()
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset())
  return now.toISOString().slice(0, 16)
}

export function inputToIso(value: string): string {
  return new Date(value).toISOString()
}

export const FINDING_LABELS: Record<string, string> = {
  PERZISTENTNI_ERITEM: 'Trajno crvenilo',
  CENTROFACIJALNA_DISTRIBUCIJA: 'Promene na sredini lica',
  TELANGIEKTAZIJE: 'Telangiektazije',
  KOMEDONI: 'Komedoni',
  PAPULE_PUSTULE_BEZ_KOMEDONA: 'Papule i pustule bez komedona',
  FLUSHING_UPITNIK: 'Flushing prema upitniku',
  FLUSHING_DNEVNIK: 'Flushing prema dnevniku',
  OKULARNI_SIMPTOMI: 'Simptomi oka',
  FIMATOZNE_PROMENE: 'Zadebljanje kože',
  PERIORALNA_DISTRIBUCIJA: 'Promene oko usta',
  PERIORALNE_PAPULE: 'Papule oko usta',
  SKORASNJI_STEROID: 'Skorašnji kortikosteroid',
}

export const GOALS = [
  { value: 'rozaceja', title: 'Rozaceja' },
  { value: 'akne', title: 'Akne' },
  { value: 'perioralni-dermatitis', title: 'Perioralni dermatitis' },
]
