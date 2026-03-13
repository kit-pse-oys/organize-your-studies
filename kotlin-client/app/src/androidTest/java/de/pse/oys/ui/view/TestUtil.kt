package de.pse.oys.ui.view

import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.ComposeTestRule
import androidx.compose.ui.test.onParent

fun ComposeTestRule.onTextInputWithLabel(label: String, substring: Boolean = true): SemanticsNodeInteraction = onNode(
    hasParent(hasSetTextAction()) and hasText(label, substring = substring),
    useUnmergedTree = true
).onParent()