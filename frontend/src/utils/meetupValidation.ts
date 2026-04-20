// ============================================================
//  meetupValidation.ts
//  Logica de validare este COMPLET separată de componente.
//  Poate fi testată independent cu Vitest.
// ============================================================

import type { CreateMeetupPayload, ValidationErrors } from '../types/indexes.ts'

// ── Constante ────────────────────────────────────────────────
export const VALIDATION_RULES = {
  LOCATION_MIN_LENGTH: 3,
  LOCATION_MAX_LENGTH: 100,
  DESCRIPTION_MAX_LENGTH: 500,
  TITLE_MIN_LENGTH: 3,
  TITLE_MAX_LENGTH: 150,
  RATING_MIN: 0,
  RATING_MAX: 5,
  DURATION_MIN: 15,
  DURATION_MAX: 480,
} as const


export function validateMeetup(payload: Partial<CreateMeetupPayload>): ValidationErrors {
  const errors: ValidationErrors = {}

  // titleEvent
  if (!payload.titleEvent || payload.titleEvent.trim().length === 0) {
    errors.titleEvent = 'Event title is required.'
  } else if (payload.titleEvent.trim().length < VALIDATION_RULES.TITLE_MIN_LENGTH) {
    errors.titleEvent = `Title must be at least ${VALIDATION_RULES.TITLE_MIN_LENGTH} characters.`
  } else if (payload.titleEvent.trim().length > VALIDATION_RULES.TITLE_MAX_LENGTH) {
    errors.titleEvent = `Title must be at most ${VALIDATION_RULES.TITLE_MAX_LENGTH} characters.`
  }

  // location
  if (!payload.location || payload.location.trim().length === 0) {
    errors.location = 'Location is required.'
  } else if (payload.location.trim().length < VALIDATION_RULES.LOCATION_MIN_LENGTH) {
    errors.location = `Location must be at least ${VALIDATION_RULES.LOCATION_MIN_LENGTH} characters.`
  } else if (payload.location.trim().length > VALIDATION_RULES.LOCATION_MAX_LENGTH) {
    errors.location = `Location cannot exceed ${VALIDATION_RULES.LOCATION_MAX_LENGTH} characters.`
  }

  // date
  if (!payload.date || payload.date.trim().length === 0) {
    errors.date = 'Date is required.'
  } else {
    const parsed = new Date(payload.date)
    if (isNaN(parsed.getTime())) {
      errors.date = 'Date is not valid.'
    } else if (parsed < new Date()) {
      errors.date = 'Date cannot be in the past.'
    }
  }

  // bookID
  if (payload.bookID === undefined || payload.bookID === null) {
    errors.bookID = 'A book must be selected.'
  } else if (!Number.isInteger(payload.bookID) || payload.bookID <= 0) {
    errors.bookID = 'Book ID must be a positive integer.'
  }

  // ownerID
  if (payload.ownerID === undefined || payload.ownerID === null) {
    errors.ownerID = 'Owner ID is required.'
  } else if (!Number.isInteger(payload.ownerID) || payload.ownerID <= 0) {
    errors.ownerID = 'Owner ID must be a positive integer.'
  }

  // ownerUsername
  if (!payload.ownerUsername || payload.ownerUsername.trim().length === 0) {
    errors.ownerUsername = 'Owner username is required.'
  }

  // duration
  if (payload.duration === undefined || payload.duration === null) {
    errors.duration = 'Duration is required.'
  } else if (
    payload.duration < VALIDATION_RULES.DURATION_MIN ||
    payload.duration > VALIDATION_RULES.DURATION_MAX
  ) {
    errors.duration = `Duration must be between ${VALIDATION_RULES.DURATION_MIN} and ${VALIDATION_RULES.DURATION_MAX} minutes.`
  }

  // rating
  if (payload.rating !== undefined && payload.rating !== null) {
    if (
      payload.rating < VALIDATION_RULES.RATING_MIN ||
      payload.rating > VALIDATION_RULES.RATING_MAX
    ) {
      errors.rating = `Rating must be between ${VALIDATION_RULES.RATING_MIN} and ${VALIDATION_RULES.RATING_MAX}.`
    }
  }


  if (payload.description && payload.description.length > VALIDATION_RULES.DESCRIPTION_MAX_LENGTH) {
    errors.description = `Description cannot exceed ${VALIDATION_RULES.DESCRIPTION_MAX_LENGTH} characters.`
  }

  return errors
}


export function hasErrors(errors: ValidationErrors): boolean {
  return Object.keys(errors).length > 0
}


export function firstError(errors: ValidationErrors): unknown {
  const first = Object.values(errors)[0]
  return first ?? null
}
