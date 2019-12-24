package me.matrix4f.cardcutter.auth

data class CardifyResult(var func: String, var status: String, var reason: String, var additional_info: String) {

    fun wasSuccessful(): Boolean {
        return status == "success"
    }

    fun getEmbeddedInfo(): HashMap<String, String> {
        val keyValPairs = additional_info.split("&")
        val result = hashMapOf<String, String>()

        for (pair in keyValPairs) {
            val splitPair = pair.split("=")
            result[splitPair[0]] = splitPair[1]
        }
        return result
    }

    override fun toString(): String {
        return "CardifyResult(func='$func', status='$status', reason='$reason', additional_info='$additional_info')"
    }
}