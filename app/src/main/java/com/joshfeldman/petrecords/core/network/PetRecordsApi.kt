package com.joshfeldman.petrecords.core.network

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Streaming

interface PetRecordsApi {
    @GET("pets")
    suspend fun getPets(
        @Query("q") query: String? = null,
        @Query("species") species: String? = null,
    ): PaginatedResponse<PetDto>

    @GET("records/search")
    suspend fun searchRecords(
        @Query("petId") petId: String? = null,
        @Query("petName") petName: String? = null,
        @Query("service") service: String? = null,
        @Query("clinicName") clinicName: String? = null,
        @Query("dateFrom") dateFrom: String? = null,
        @Query("dateTo") dateTo: String? = null,
        @Query("invoiceNumber") invoiceNumber: String? = null,
        @Query("minTotal") minTotal: Double? = null,
        @Query("maxTotal") maxTotal: Double? = null,
        @Query("text") text: String? = null,
    ): PaginatedResponse<SearchVisitDto>

    @GET("documents/{id}")
    suspend fun getDocument(@Path("id") id: String): DocumentRecordDto

    @DELETE("documents/{id}")
    suspend fun deleteDocument(@Path("id") id: String)

    @Streaming
    @GET("documents/{id}/file")
    suspend fun downloadDocument(@Path("id") id: String): Response<ResponseBody>

    @GET("weights/pets/{petId}")
    suspend fun getWeightSeries(@Path("petId") petId: String): WeightSeriesDto

    @Multipart
    @POST("documents/upload")
    suspend fun uploadDocument(
        @Part file: MultipartBody.Part,
        @Part("petId") petId: RequestBody? = null,
        @Part("clinicId") clinicId: RequestBody? = null,
        @Part("visitDate") visitDate: RequestBody? = null,
    ): UploadResponse

    @Multipart
    @POST("documents/upload-images")
    suspend fun uploadDocumentImages(
        @Part files: List<MultipartBody.Part>,
        @Part("petId") petId: RequestBody? = null,
        @Part("clinicId") clinicId: RequestBody? = null,
        @Part("visitDate") visitDate: RequestBody? = null,
        @Part("ocrPageCount") ocrPageCount: RequestBody? = null,
    ): UploadResponse
}
