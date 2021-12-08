# CustomLayoutManager

> https://juejin.cn/post/6870770285247725581
> https://wiresareobsolete.com/2014/09/building-a-recyclerview-layoutmanager-part-1/

1. 重写 `onMeasure` or `isAutoMeasure`
2. 重写 `onLayoutChildren` 进行布局
3. 重写 `canScrollXXX` 和 `scrollXXXBy`，实现滑动布局
4. 重写 `scrollToPosition()` 和 `smoothScrollToPosition()` 实现跳转功能