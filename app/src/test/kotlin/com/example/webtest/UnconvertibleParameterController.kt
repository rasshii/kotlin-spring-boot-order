package com.example.webtest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * 文字列から変換できない型の引数を持つコントローラ。Spring MVC は引数を作れず、ResponseEntityExceptionHandler が
 * 500 で返す例外 (ConversionNotSupportedException) を投げる。
 *
 * com.example.shop の外に置き、使うテストだけが @Import で読み込む。中に置くと、アプリの component scan が
 * ほかのテストの context にも登録する。
 */
@RestController
class UnconvertibleParameterController {

    class Unconvertible

    @GetMapping("/test/unconvertible")
    fun read(@RequestParam value: Unconvertible): String = value.toString()
}
