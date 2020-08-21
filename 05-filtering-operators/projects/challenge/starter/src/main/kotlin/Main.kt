import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.subjects.PublishSubject

fun main(args: Array<String>) {

  exampleOf("Challenge 1") {

    val subscriptions = CompositeDisposable()

    val contacts = mapOf(
        "603-555-1212" to "Florent",
        "212-555-1212" to "Junior",
        "408-555-1212" to "Marin",
        "617-555-1212" to "Scott")

    fun phoneNumberFrom(inputs: List<Int>): String {
      val phone = inputs.map { it.toString() }.toMutableList()
      phone.add(3, "-")
      phone.add(7, "-")
      return phone.joinToString("")
    }

    val input = PublishSubject.create<Int>()

    // Add your code here


    input.onNext(0)
    input.onNext(603)

    input.onNext(2)
    input.onNext(1)

    // Confirm that 7 results in "Contact not found", and then change to 2 and confirm that Junior is found
    input.onNext(7)

    "5551212".forEach {
      input.onNext(it.toString().toInt()) // Need toString() or else Char conversion is done
    }

    input.onNext(9)
  }
}