package xyz.techmush.birdhouse_peeper.model

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton // avoid recreation; application global
    fun provideBirdhouseRepository(@ApplicationContext context: Context) = BirdhouseRepository(context)
}
