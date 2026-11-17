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

export type Condition =
  | 'ACNE'
  | 'ROSACEA'
  | 'PERIORAL_DERMATITIS'
  | 'ATOPIC_DERMATITIS'
  | 'SEBORRHEIC_DERMATITIS'
  | 'CONTACT_DERMATITIS'
export type Severity = 'MILD' | 'MODERATE' | 'SEVERE' | 'VERY_SEVERE'
export type RosaceaSubtype = 'ETR' | 'PPR' | 'PHYMATOUS' | 'OCULAR'
export type RecommendationStatus = 'ACTIVE' | 'PENDING_REVIEW' | 'APPROVED' | 'REJECTED' | 'WITHDRAWN'
export type RoutineSlot = 'MORNING' | 'EVENING'
export type RoutineStep = 'CLEANSING' | 'TREATMENT' | 'HYDRATION' | 'SPF'

export interface Diagnosis {
  condition: Condition
  severity: Severity | null
  subtype: RosaceaSubtype | null
  score: number | null
}

export interface ExcludedDiagnosis {
  condition: Condition
  reason: string
}

export interface Recommendation {
  id: number
  origin: 'ANALYSIS' | 'CEP'
  forCondition: Condition | null
  sastojak: string
  koncentracija: number | null
  form: string
  slot: RoutineSlot
  step: RoutineStep
  frequency: string
  requiresPrescription: boolean
  status: RecommendationStatus
  reason: string
  source: string
  ruleName: string
  warnings: string[]
  reviewComment: string | null
  createdAt: string
}

export interface Referral {
  forCondition: Condition
  specialist: string
  reason: string
  source: string
}

export interface SafetyNote {
  text: string
  source: string
}

export interface ExplanationStep {
  ruleName: string
  agendaGroup: string
  source: string
  description: string
}

export interface AnalysisResponse {
  id: number
  createdAt: string
  fototip: number
  med: number
  diagnoses: Diagnosis[]
  excludedDiagnoses: ExcludedDiagnosis[]
  morningRoutine: Recommendation[]
  eveningRoutine: Recommendation[]
  notInRoutine: Recommendation[]
  referrals: Referral[]
  safetyNotes: SafetyNote[]
  contactDermatitisCauses: string[]
  explanation: ExplanationStep[]
}

export type FlushTrigger = 'HRANA' | 'ALKOHOL' | 'TOPLOTA' | 'STRES' | 'KOZMETIKA'
export type SymptomType = 'ERYTHEMA' | 'STINGING' | 'ITCHING' | 'SCALING'
export type DiaryEntryType = 'FLUSHING' | 'SUN' | 'NEW_PRODUCT' | 'SYMPTOM'

export interface DiaryEntry {
  id: number
  type: DiaryEntryType
  occurredAt: string
  durationMinutes: number | null
  trigger: FlushTrigger | null
  uvIndex: number | null
  minutesInSun: number | null
  sunscreenSpf: number | null
  productName: string | null
  symptomType: SymptomType | null
  symptomLocation: BodyArea | null
}

export interface CepAlert {
  id: number
  type: string
  message: string
  occurredAt: string
  seen: boolean
}

export interface DiaryEntryResponse {
  entry: DiaryEntry
  alerts: CepAlert[]
  recommendations: Recommendation[]
}

export interface HypothesisResponse {
  goal: string
  query: string
  proven: boolean
  provenThrough: string[]
}

export interface ContactCause {
  sastojak: string
  proizvod: string
}

export interface AllergenCheck {
  sastojak: string
  knownAllergen: boolean
  groups: string[]
}

export interface PatientSummary {
  id: number
  fullName: string
  email: string
  questionnaireFilled: boolean
  fototip: number
  med: number
  pregnant: boolean
  lastAnalysisAt: string | null
  pendingReviews: number
}

export interface PendingRecommendation {
  patientId: number
  patientName: string
  pregnant: boolean
  recommendation: Recommendation
}

export interface TemplateRule {
  id: number
  sastojak: string
  minFototip: number
  maxFototip: number
  maxKoncentracija: number
  napomena: string
  updatedBy: string
  updatedAt: string
}

export interface TemplateRuleRequest {
  sastojak: string
  minFototip: number
  maxFototip: number
  maxKoncentracija: number
  napomena: string
}

export interface UserRow {
  id: number
  email: string
  fullName: string
  role: Role
  active: boolean
  createdAt: string
}

export interface RuleInfo {
  name: string
  agendaGroup: string
  source: string
  opis: string
}

export interface KnowledgeBase {
  name: string
  rules: number
  queries: number
  rulesPerAgendaGroup: Record<string, number>
  ruleList: RuleInfo[]
  queryList: string[]
}

export interface KnowledgeBaseInfo {
  version: number
  rebuiltAt: string
  templateRows: number
  openCepSessions: number
  bases: KnowledgeBase[]
}

export type IngredientKind = 'THERAPY' | 'INGREDIENT' | 'GROUP' | 'MIXTURE'

export interface IngredientOption {
  name: string
  kind: IngredientKind
  partOf: string[]
}
