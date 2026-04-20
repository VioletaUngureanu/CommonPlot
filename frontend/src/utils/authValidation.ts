// ============================================================
//  utils/authValidation.ts
//  Validare pentru login și register — separată de UI.
// ============================================================

export interface RegisterPayload {
  fullName: string
  username: string
  email: string
  password: string
  confirmPassword: string
}

export interface LoginPayload {
  email: string
  password: string
}

export type RegisterErrors = Partial<Record<keyof RegisterPayload, string>>
export type LoginErrors    = Partial<Record<keyof LoginPayload, string>>

// ── Register ─────────────────────────────────────────────────
export function validateRegister(payload: Partial<RegisterPayload>): RegisterErrors {
  const errors: RegisterErrors = {}

  // fullName
  if (!payload.fullName || payload.fullName.trim().length === 0) {
    errors.fullName = 'Full name is required.'
  } else if (payload.fullName.trim().length < 2) {
    errors.fullName = 'Full name must be at least 2 characters.'
  } else if (payload.fullName.trim().length > 100) {
    errors.fullName = 'Full name must be at most 100 characters.'
  }

  // username
  if (!payload.username || payload.username.trim().length === 0) {
    errors.username = 'Username is required.'
  } else if (payload.username.length < 3) {
    errors.username = 'Username must be at least 3 characters.'
  } else if (payload.username.length > 30) {
    errors.username = 'Username must be at most 30 characters.'
  } else if (!/^[a-zA-Z0-9_]+$/.test(payload.username)) {
    errors.username = 'Username can only contain letters, numbers and underscores.'
  }

  // email
  if (!payload.email || payload.email.trim().length === 0) {
    errors.email = 'Email is required.'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.email)) {
    errors.email = 'Please enter a valid email address.'
  }

  // password
  if (!payload.password || payload.password.length === 0) {
    errors.password = 'Password is required.'
  } else if (payload.password.length < 6) {
    errors.password = 'Password must be at least 6 characters.'
  } else if (payload.password.length > 100) {
    errors.password = 'Password is too long.'
  }

  // confirmPassword
  if (!payload.confirmPassword || payload.confirmPassword.length === 0) {
    errors.confirmPassword = 'Please confirm your password.'
  } else if (payload.password !== payload.confirmPassword) {
    errors.confirmPassword = 'Passwords do not match.'
  }

  return errors
}

// ── Login ─────────────────────────────────────────────────────
export function validateLogin(payload: Partial<LoginPayload>): LoginErrors {
  const errors: LoginErrors = {}

  if (!payload.email || payload.email.trim().length === 0) {
    errors.email = 'Email is required.'
  } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.email)) {
    errors.email = 'Please enter a valid email address.'
  }

  if (!payload.password || payload.password.length === 0) {
    errors.password = 'Password is required.'
  }

  return errors
}

// ── Helpers ───────────────────────────────────────────────────
export function hasAuthErrors(errors: RegisterErrors | LoginErrors): boolean {
  return Object.keys(errors).length > 0
}
