package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// ============================================================================
// LAVISH LUXURY PALETTE (Imperial Royal Indigo, Sovereign Gold & Jewel Tones)
// ============================================================================

// Primary: Imperial Royal Indigo & Radiant Amethyst
val PrimaryBlue = Color(0xFF4338CA)          // Opulent Royal Indigo
val PrimaryBlueLight = Color(0xFF6366F1)     // Electric Indigo
val PrimaryBlueDark = Color(0xFF1E1B4B)      // Midnight Imperial Velvet
val PrimaryBlueContainer = Color(0xFFEEF2FF) // Silken Lilac Tint
val OnPrimaryBlueContainer = Color(0xFF312E81)

// Secondary: Imperial Emerald / Jade
val SecondaryTeal = Color(0xFF059669)        // Royal Emerald
val SecondaryTealLight = Color(0xFF10B981)   // Radiant Jade
val SecondaryTealContainer = Color(0xFFD1FAE5)
val OnSecondaryTealContainer = Color(0xFF064E3B)

// Tertiary: Majestic Amethyst & Royal Purple
val TertiaryIndigo = Color(0xFF7C3AED)       // Imperial Amethyst
val TertiaryContainer = Color(0xFFF3E8FF)     // Velvet Orchid
val OnTertiaryContainer = Color(0xFF4C1D95)

// Sovereign Gold & Champagne Accents (Pure Luxury)
val LavishGold = Color(0xFFD97706)           // Sovereign Amber Gold
val LavishGoldLight = Color(0xFFF59E0B)      // Radiant Gold
val LavishGoldWarm = Color(0xFFFBBF24)       // Gleaming Gold
val LavishChampagne = Color(0xFFFEF3C7)      // Warm Champagne Silk
val LavishChampagneBorder = Color(0xFFFDE68A)// Champagne Border Highlight
val OnLavishGold = Color(0xFF78350F)         // Deep Bronze

// Jewel Tones for Categorization & Status
val PriorityLowColor = Color(0xFF059669)     // Imperial Emerald (Low)
val PriorityMediumColor = Color(0xFFD97706)  // Topaz Amber (Medium)
val PriorityHighColor = Color(0xFFEA580C)    // Fiery Coral (High)
val PriorityCriticalColor = Color(0xFFBE123C)// Velvet Ruby (Critical)

val StatusPending = Color(0xFFD97706)        // Amber Topaz
val StatusAssigned = Color(0xFF2563EB)       // Royal Sapphire
val StatusInProgress = Color(0xFF7C3AED)     // Majestic Amethyst
val StatusResolved = Color(0xFF059669)       // Imperial Emerald

// Hostel Residence Jewel Badges
val BoysHostelSapphire = Color(0xFF1D4ED8)
val BoysHostelContainer = Color(0xFFEFF6FF)
val GirlsHostelRuby = Color(0xFFBE185D)
val GirlsHostelContainer = Color(0xFFFDF2F8)
val ApartmentAmethyst = Color(0xFF6B21A8)
val ApartmentContainer = Color(0xFFF3E8FF)

// Opulent Neutral Surfaces (Porcelain & Alabaster Silk)
val BackgroundLight = Color(0xFFFAF8FE)      // Opulent Pearl Silk Canvas
val SurfaceLight = Color(0xFFFFFFFF)         // Pure Porcelain
val SurfaceVariantLight = Color(0xFFF5F3FF)  // Soft Orchid Tint
val OutlineLight = Color(0xFFE2E8F0)         // Refined Silver
val OutlineGold = Color(0xFFFDE68A)          // Champagne Gold Border
val OutlinePurple = Color(0xFFDDD6FE)        // Amethyst Sheen Border

// Dark Palette (Midnight Obsidian & Radiant Violet)
val BackgroundDark = Color(0xFF0B091A)       // Deep Obsidian Night
val SurfaceDark = Color(0xFF161233)          // Midnight Velvet Surface
val SurfaceVariantDark = Color(0xFF231C4D)   // Royal Indigo Dark
val OutlineDark = Color(0xFF3B3270)

// ============================================================================
// SIGNATURE LAVISH GRADIENT BRUSHES
// ============================================================================

val LavishRoyalGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF1E1B4B), // Midnight Imperial
        Color(0xFF312E81), // Deep Royal Velvet
        Color(0xFF4338CA), // Imperial Royal Indigo
        Color(0xFF7C3AED)  // Majestic Amethyst
    )
)

val LavishGoldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFFB45309), // Deep Amber Gold
        Color(0xFFD97706), // Sovereign Gold
        Color(0xFFF59E0B), // Lustrous Gold
        Color(0xFFFBBF24)  // Radiant Champagne
    )
)

val LavishSunsetGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF4338CA), // Imperial Indigo
        Color(0xFF7C3AED), // Royal Amethyst
        Color(0xFFDB2777)  // Velvet Rose
    )
)

val LavishEmeraldGradient = Brush.linearGradient(
    colors = listOf(
        Color(0xFF064E3B), // Deep Forest Jade
        Color(0xFF059669), // Imperial Emerald
        Color(0xFF10B981)  // Radiant Jade
    )
)

val LavishHeroSurfaceGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFFFAF8FE),
        Color(0xFFF5F0FF)
    )
)
