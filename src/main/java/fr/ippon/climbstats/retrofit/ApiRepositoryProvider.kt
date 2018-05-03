package fr.ippon.climbstats.retrofit

object ApiRepositoryProvider {
    fun provideRepository(): Api {
        return Api(ApiService.Factory.create())
    }
}