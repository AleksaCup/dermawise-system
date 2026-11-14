import 'vuetify/styles'
import { createVuetify } from 'vuetify'
import { aliases, mdi } from 'vuetify/iconsets/mdi-svg'

export default defineNuxtPlugin((nuxtApp) => {
  const vuetify = createVuetify({
    theme: {
      defaultTheme: 'dermawise',
      themes: {
        dermawise: {
          dark: false,
          colors: {
            primary: '#0E7490',
            'on-primary': '#FFFFFF',
            background: '#F8FAFC',
            surface: '#FFFFFF',
            'on-surface': '#0F172A',
            'on-background': '#0F172A',
            success: '#047857',
            warning: '#B45309',
            error: '#B91C1C',
            info: '#1D4ED8',
          },
          variables: {
            'border-color': '#0F172A',
            'border-opacity': 0.12,
          },
        },
      },
    },
    icons: {
      defaultSet: 'mdi',
      aliases,
      sets: { mdi },
    },
    defaults: {
      VCard: { elevation: 0, border: true },
      VAppBar: { elevation: 0, border: 'b' },
      VBtn: { elevation: 0 },
      VTextField: { variant: 'outlined', density: 'comfortable', color: 'primary' },
      VTextarea: { variant: 'outlined', density: 'comfortable', color: 'primary' },
      VSelect: { variant: 'outlined', density: 'comfortable', color: 'primary' },
      VCombobox: { variant: 'outlined', density: 'comfortable', color: 'primary' },
      VCheckbox: { color: 'primary', density: 'comfortable', hideDetails: 'auto' },
      VSwitch: { color: 'primary', density: 'comfortable', inset: true, hideDetails: 'auto' },
      VChip: { size: 'small', variant: 'tonal' },
      VAlert: { variant: 'tonal', density: 'comfortable' },
      VTable: { density: 'comfortable' },
    },
  })

  nuxtApp.vueApp.use(vuetify)
})
