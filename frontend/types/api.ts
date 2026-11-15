export interface ApiErrorBody {
  timestamp: string
  status: number
  error: string
  message: string
  fieldErrors: Record<string, string>
}

export type Role = 'USER' | 'DERMATOLOGIST' | 'ADMIN'

export interface AuthResponse {
  token: string
  userId: number
  email: string
  fullName: string
  role: Role
}

export type SkinColor = 'VERY_FAIR' | 'FAIR' | 'LIGHT_BROWN' | 'OLIVE' | 'DARK_BROWN' | 'BLACK'
export type EyeColor = 'BLUE' | 'GREY' | 'GREEN' | 'HAZEL' | 'BROWN' | 'DARK_BROWN'
export type HairColor = 'RED' | 'BLONDE' | 'LIGHT_BROWN' | 'DARK_BROWN' | 'BLACK'
export type EthnicOrigin =
  | 'NORTHERN_EUROPEAN'
  | 'SOUTHERN_EUROPEAN'
  | 'MIDDLE_EASTERN'
  | 'SOUTH_ASIAN'
  | 'EAST_ASIAN'
  | 'LATIN_AMERICAN'
  | 'AFRICAN'
  | 'OTHER'
export type SunReaction = 'ALWAYS_BURNS' | 'BURNS_THEN_TANS' | 'RARELY_BURNS' | 'NEVER_BURNS'
export type TanTendency = 'NEVER' | 'MINIMAL' | 'GRADUAL' | 'EASILY' | 'DEEPLY'
export type FaceRegion = 'FOREHEAD' | 'RIGHT_CHEEK' | 'LEFT_CHEEK' | 'NOSE' | 'CHIN' | 'CHEST_BACK'
export type LesionType = 'NONE' | 'COMEDONES' | 'PAPULES' | 'PUSTULES' | 'NODULES'
export type BodyArea =
  | 'FOREHEAD'
  | 'CHEEKS'
  | 'NOSE'
  | 'CHIN'
  | 'PERIORAL'
  | 'NASOLABIAL_FOLDS'
  | 'EYEBROWS'
  | 'EYELIDS'
  | 'SCALP'
  | 'BEHIND_EARS'
  | 'NECK'
  | 'CHEST_BACK'
  | 'HANDS'
  | 'BODY'

export interface IngredientDto {
  name: string
  concentration: number | null
}

export interface ProductDto {
  name: string
  ingredients: IngredientDto[]
  applicationAreas: BodyArea[]
}

export interface Questionnaire {
  phototype: {
    skinColor: SkinColor | null
    eyeColor: EyeColor | null
    hairColor: HairColor | null
    ethnicOrigin: EthnicOrigin | null
    sunReaction: SunReaction | null
    tanTendency: TanTendency | null
  }
  lesions: Record<FaceRegion, LesionType>
  rosacea: {
    persistentErythema: boolean
    centrofacialDistribution: boolean
    flushingPerWeek: number
    telangiectasia: boolean
    papulesPustulesWithoutComedones: boolean
    ocularSymptoms: boolean
    phymatousChanges: boolean
    flushingGrade: number
    persistentErythemaGrade: number
    telangiectasiaGrade: number
    papulesPustulesGrade: number
    papuleCount: number
    pustuleCount: number
    perioralDistribution: boolean
    recentTopicalSteroid: boolean
  }
  scorad: {
    affectedAreaPercent: number
    erythema: number
    edemaPapulation: number
    excoriation: number
    lichenification: number
    oozingCrusting: number
    dryness: number
    pruritus: number
    sleeplessness: number
  }
  lifestyle: {
    pregnant: boolean
    breastfeeding: boolean
    hormonalTherapy: boolean
    allergies: string[]
    currentProducts: ProductDto[]
    dailySunMinutes: number
  }
}

export interface ProfileResponse {
  userId: number
  fullName: string
  questionnaire: Questionnaire
  fototip: number
  med: number
  pregnant: boolean
  breastfeeding: boolean
  updatedAt: string
}
