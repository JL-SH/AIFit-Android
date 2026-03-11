package com.jlsh.aifit.feature.education.domain.model

data class GlossaryDefinition(
    val term: String,
    val definition: String,
    val category: String,
    val relatedTerms: List<String>,
)

