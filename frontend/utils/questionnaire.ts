import type { Questionnaire } from '~/types/api'

export function emptyQuestionnaire(): Questionnaire {
  return {
    phototype: {
      skinColor: null,
      eyeColor: null,
      hairColor: null,
      ethnicOrigin: null,
      sunReaction: null,
      tanTendency: null,
    },
    lesions: {
      FOREHEAD: 'NONE',
      RIGHT_CHEEK: 'NONE',
      LEFT_CHEEK: 'NONE',
      NOSE: 'NONE',
      CHIN: 'NONE',
      CHEST_BACK: 'NONE',
    },
    rosacea: {
      persistentErythema: false,
      centrofacialDistribution: false,
      flushingPerWeek: 0,
      telangiectasia: false,
      papulesPustulesWithoutComedones: false,
      ocularSymptoms: false,
      phymatousChanges: false,
      flushingGrade: 0,
      persistentErythemaGrade: 0,
      telangiectasiaGrade: 0,
      papulesPustulesGrade: 0,
      papuleCount: 0,
      pustuleCount: 0,
      perioralDistribution: false,
      recentTopicalSteroid: false,
    },
    scorad: {
      affectedAreaPercent: 0,
      erythema: 0,
      edemaPapulation: 0,
      excoriation: 0,
      lichenification: 0,
      oozingCrusting: 0,
      dryness: 0,
      pruritus: 0,
      sleeplessness: 0,
    },
    lifestyle: {
      pregnant: false,
      breastfeeding: false,
      hormonalTherapy: false,
      allergies: [],
      currentProducts: [],
      dailySunMinutes: 0,
    },
  }
}
