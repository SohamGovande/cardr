package me.sohamgovande.cardr.util

import me.sohamgovande.cardr.core.card.Author

class AuthorList(val value: Array<Author>) {

    constructor(single: Author): this(arrayOf(single))

}