package com.example.demo

import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@SpringBootApplication
class DemoApplication

fun main(args: Array<String>) {
    SpringApplication.run(DemoApplication::class.java, *args)
}

@Component
class InsertBenchmark(val jdbcTemplate: JdbcTemplate) : CommandLineRunner {
    val batchSize = 1000

    override fun run(vararg args: String?) {
        createSchema()

        val items = makeStuff()

        val start = System.currentTimeMillis()

        println("\nInserting items...")

        insertAll(items)

        val duration = System.currentTimeMillis() - start

        println("Inserted ${items.size} items in ${duration}ms\n")
    }

    fun createSchema() {
        jdbcTemplate.update("""
			CREATE TABLE IF NOT EXISTS stuff (
				id bigserial primary key,
				project_id bigint,
				uri text,
				writable boolean
			);
		""")
    }

    fun makeStuff(): List<Stuff> {
        val items = mutableListOf<Stuff>()

        for (i in 1..100000) {
            val uri = "http://prefix.com/sufficiently-long-item-uri#and-a-random-suffix-${UUID.randomUUID()}"
            val writable = uri.hashCode() % 2 == 0
            items.add(Stuff(uri, 42, writable))
        }

        return items
    }

    fun insertAll(items: List<Stuff>) {
        jdbcTemplate.batchUpdate(
                "INSERT INTO stuff (project_id, uri, writable) VALUES (?, ?, ?)",
                items,
                batchSize,
                { ps, item ->
                    ps.setLong(1, item.projectId)
                    ps.setString(2, item.uri)
                    ps.setBoolean(3, item.writable)
                }
        )
    }
}

data class Stuff(val uri: String, val projectId: Long, val writable: Boolean)
