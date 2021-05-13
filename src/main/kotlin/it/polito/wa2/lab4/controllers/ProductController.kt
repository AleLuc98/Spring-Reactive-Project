package it.polito.wa2.lab4.controllers

import it.polito.wa2.lab4.dto.ProductDTO
import it.polito.wa2.lab4.services.ProductService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.*
import reactor.kotlin.core.publisher.toMono
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive
import kotlin.RuntimeException

@RestController
@RequestMapping("/warehouse")
@Validated
class ProductController(val productService: ProductService) {

    @PostMapping("/products")
    suspend fun addProduct(
            @RequestBody
            // @Valid
            bodyDTO: ProductDTO): ResponseEntity<Any> {

        return try {
            val newProduct = productService.addProduct(bodyDTO.name,
                                                       bodyDTO.category,
                                                       bodyDTO.price,
                                                       bodyDTO.quantity
            )
            ResponseEntity(newProduct, HttpStatus.CREATED)

        } catch(e: Exception){
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @PatchMapping("/products/{productId}")
    suspend fun updateQuantity(@PathVariable
                               @Positive(message = "Insert a valid productId")
                               productId: Long,

                               @RequestBody(required = true)
                               @NotNull(message = "Insert a valid quantity")
                               bodyVal: Long? = null
    ): ResponseEntity<Any> {

        return try{
            //println("New quantity: ${bodyVal}")
            val newProduct = productService.updateProductQuantity(productId, bodyVal!!)
            ResponseEntity(newProduct, HttpStatus.OK)
        } catch(e: Exception){
            //println("EXCEPTION")
            ResponseEntity(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/products/{productId}")
    suspend fun getProduct(@PathVariable
                           @Positive(message = "Insert a valid productId")
                           productId: Long
    ): ResponseEntity<Any> {

        return try{
            val returnPrd = productService.getProduct(productId).awaitSingle()
            ResponseEntity<Any>(returnPrd, HttpStatus.OK)
        }
        catch (e: Exception){
            ResponseEntity<Any>(e.message, HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/products")
    suspend fun getAllProducts(): ResponseEntity<Flow<Any>> {

        return try {
            ResponseEntity<Flow<Any>>(productService.getAllProduct(), HttpStatus.OK
            )
        } catch(e: Exception){
            ResponseEntity<Flow<Any>>(e.message.toMono().asFlow(), HttpStatus.BAD_REQUEST)
        }
    }

    @GetMapping("/productsByCategory")
    suspend fun getProductsByCategory(@RequestParam(name = "category", required = true)
                                      @NotNull(message = "A category is required")
                                      category: String? = null
    ): ResponseEntity<Any> {

        return try{
            if(category == null)
                throw RuntimeException("Insert a category name")

            val products = productService.getProductsByCategory(category)

            ResponseEntity<Any>(products, HttpStatus.OK)
        }
        catch(e: Exception){
            ResponseEntity<Any>(e.message, HttpStatus.BAD_REQUEST)
        }
    }

}