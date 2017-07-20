package me.jbusdriver.ui.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v4.widget.DrawerLayout
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.widget.Toolbar
import android.view.MenuItem
import jbusdriver.me.jbusdriver.R
import me.jbusdriver.common.AppBaseActivity
import me.jbusdriver.common.BaseFragment
import me.jbusdriver.common.KLog
import me.jbusdriver.mvp.MainContract
import me.jbusdriver.mvp.presenter.MainPresenterImpl
import me.jbusdriver.ui.data.DataSourceType
import me.jbusdriver.ui.fragment.HomeMovieListFragment
import me.jbusdriver.ui.fragment.MineCollectFragment

class MainActivity : AppBaseActivity<MainContract.MainPresenter, MainContract.MainView>(), NavigationView.OnNavigationItemSelectedListener, MainContract.MainView {

    private val navigationView by lazy { findViewById(R.id.nav_view) as NavigationView }
    private lateinit var selectMenu: MenuItem
    private val fragments: Map<Int, BaseFragment> by lazy {
        mapOf(
                R.id.mine_collect to MineCollectFragment.newInstance() as BaseFragment,
                R.id.movie_ma to HomeMovieListFragment.newInstance(DataSourceType.CENSORED),
                R.id.movie_uncensored to HomeMovieListFragment.newInstance(DataSourceType.UNCENSORED),
                R.id.movie_xyz to HomeMovieListFragment.newInstance(DataSourceType.XYZ),
                R.id.movie_hd to HomeMovieListFragment.newInstance(DataSourceType.GENRE_HD),
                R.id.movie_sub to HomeMovieListFragment.newInstance(DataSourceType.Sub)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        val toggle = ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()
        initFragments()
        val menuId = savedInstanceState?.getInt("MenuSelectedItemId", R.id.movie_ma) ?: R.id.movie_ma
        navigationView.setNavigationItemSelectedListener(this)
        selectMenu = navigationView.menu.findItem(menuId)
        navigationView.setCheckedItem(selectMenu.itemId)
        onNavigationItemSelected(selectMenu)
    }


    private fun initFragments() {
        val ft = supportFragmentManager.beginTransaction()
        fragments.forEach {
            (k, v) ->
            ft.add(R.id.content_main, v, k.toString()).hide(v)
        }
        ft.commit()
    }


    override fun onPostResume() {
        super.onPostResume()

    }

    override fun onBackPressed() {
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
            //   moveTaskToBack(false)
        }
    }


    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        switchFragment(item.itemId)
        selectMenu = item
        KLog.d("onNavigationItemSelected $item ")
        val drawer = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer.closeDrawer(GravityCompat.START)
        supportActionBar?.title = selectMenu.title
        return true
    }

    private fun switchFragment(itemId: Int) {
        val replace = fragments[itemId] ?: error("no match fragment for menu $itemId")

        val ft = supportFragmentManager.beginTransaction()
        supportFragmentManager.findFragmentByTag(selectMenu.itemId.toString())?.let {
            ft.hide(it)
        }
        ft.show(replace)
        ft.commitAllowingStateLoss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("MenuSelectedItemId", selectMenu.itemId)
        super.onSaveInstanceState(outState)
    }

    override fun createPresenter() = MainPresenterImpl()

    override val layoutId = R.layout.activity_main

    companion object {
        fun start(current: Activity) {
            current.startActivity(Intent(current, MainActivity::class.java))
        }
    }
}
