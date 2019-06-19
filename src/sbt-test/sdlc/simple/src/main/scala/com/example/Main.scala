package com.example

class Main {
  /** method doc */
  def foo(x: Int): Int = x
  def a[T](x: T): T = x
  def a[K, V](x: (K, V)): (K, V) = x
  def / = 1
  def `"` = 2
  def + = 3
  def % = 4
  def `%22` = 5
  def `""` = 6

  class Inner {
    def innerM(x: Int): Int = x
  }

  type InnerType

  object InnerObject
}

object Main {
  class NestedClass

  object NestedObject

  type NestedType

  def foo: String = ""
}
