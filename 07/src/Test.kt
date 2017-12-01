import java.util.*

/**
 * http://blog.csdn.net/cow__sky/article/details/8276508
 * 逆序数:https://baike.baidu.com/item/%E9%80%86%E5%BA%8F%E6%95%B0/3334502?fr=aladdin
 * https://zhuanlan.zhihu.com/p/28039855
 *
 * A*算法是一种启发式搜索算法,启发中的估价是用估价函数表示的，如：f(n) = g(n) + h(n)　
 * 　其中f(n) 是节点n的估价函数，g(n)是在状态空间中从初始节点到n节点的实际代价，
 *   h(n)是从n到目标节点最佳路径的估计代价。 在此八数码问题中，显然g(n)就是从初始状态变换到当前状态所移动的步数，
 *   估计代价h(n)我们就可采用当前状态各个数字牌不在目标状态未知的个数，即错位数。
 */
fun main(args: Array<String>) {
  print(String.format("123123%s","abcdefg"))
}