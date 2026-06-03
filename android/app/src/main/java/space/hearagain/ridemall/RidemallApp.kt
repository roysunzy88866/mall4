package space.hearagain.ridemall

import android.app.Application
import space.hearagain.ridemall.data.ServiceLocator

/**
 * 应用入口。装配轻量 ServiceLocator(Retrofit / Repository / 设备号存储)。
 * 依赖:Android Application。暴露:全局依赖装配。
 */
class RidemallApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
