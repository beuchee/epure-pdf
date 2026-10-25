package com.alain.epure.pdf.domain

// Occurrence shown after « Suivant »: the next one, going back to the first after the last (SPEC F-11.3).
fun nextOccurrence(current: Int, count: Int): Int = if (count == 0) 0 else (current + 1) % count

// Occurrence shown after « Précédent »: the previous one, going to the last before the first (SPEC F-11.3).
fun previousOccurrence(current: Int, count: Int): Int = if (count == 0) 0 else (current - 1 + count) % count
