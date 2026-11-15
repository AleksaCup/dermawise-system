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
