
import java.io.IOException
import jdk.nashorn.internal.runtime.ScriptingFunctions.readLine
import java.io.FileReader
import java.io.BufferedReader
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
    val stnum = intArrayOf(2, 1, 6, 4, 0, 8, 7, 5, 3)
    val tanum = intArrayOf(1, 2, 3, 8, 0, 4, 7, 6, 5)
    EightPuzzle().run(stnum ,tanum )
}

/**
 * A*算法解决八数码问题 Java语言实现
 */
class EightPuzzle : Comparable<Any> {
        var num = IntArray(9)  //当前的盘面状态
        var depth: Int = 0                    //当前的深度即走到当前状态的步骤
        var evaluation: Int = 0               //从起始状态到目标的最小估计值
        var misposition: Int = 0              //到目标的最小估计
        var parent: EightPuzzle? = null      //当前状态的父状态

        /**
         * 判断当前状态是否为目标状态
         * @param target
         * @return
         */
        fun isTarget(target: EightPuzzle): Boolean {
            return Arrays.equals(num, target.num)
        }

        /**
         * 求f(n) = g(n)+h(n);
         * 初始化状态信息
         * @param target
         */
        fun init(target: EightPuzzle) {
            var temp = 0
            for (i in 0..8) {
                if (num[i] != target.num[i])
                    temp++
            }
            this.misposition = temp
            if (this.parent == null) {
                this.depth = 0
            } else {
                this.depth = this.parent!!.depth + 1
            }
            this.evaluation = this.depth + this.misposition
        }

        /**
         * 对于八数码问题的解决，首先要考虑是否有答案。每一个状态可认为是一个1×9的矩阵，问题即通过矩阵的变换，
         * 是否可以变换为目标状态对应的矩阵？由数学知识可知，可计算这两个有序数列的逆序值，如果两者都是偶数或奇数，
         * 则可通过变换到达，否则，这两个状态不可达。这样，就可以在具体解决问题之前判断出问题是否可解，从而可以避免不必要的搜索。
         * 原理:因为每次都是两个数值交换位置,其大小值会互换.如果两个数列像同一个数列A转换,那么一个转换X次,一个是X+1,这这两个数列
         * 无法通过变换得到.
         * 求逆序值并判断是否有解
         * @param target
         * @return 有解：true 无解：false
         */
        fun isSolvable(target: EightPuzzle): Boolean {
            var reverse = 0
            for (i in 0..8) {//num[i]是后面的数
                for (j in 0..i - 1) {//num[j]是前面的数
                    if (num[j] > num[i])
                        reverse++
                    if (target.num[j] > target.num[i])
                        reverse++
                }
            }
            return if (reverse % 2 == 0) true else false
        }

        override operator fun compareTo(other: Any): Int {
            if (other is EightPuzzle){
                return this.evaluation - other.evaluation//默认排序为f(n)由小到大排序
            }
            return -1;
        }

        /**
         * @return 返回0在八数码中的位置
         */
        val zeroPosition: Int
            get() {
                var position = -1
                for (i in 0..8) {
                    if (this.num[i] == 0) {
                        position = i
                    }
                }
                return position
            }

        /**
         *
         * @param open    状态集合
         * @return 判断当前状态是否存在于open表中
         */
        fun isContains(open: ArrayList<EightPuzzle>): Int {
            for (i in open.indices) {
                if (Arrays.equals(open[i].num, num)) {
                    return i
                }
            }
            return -1
        }

        /**
         *
         * @return 小于3的不能上移返回false
         */
        val isMoveUp: Boolean
            get() {
                val position = zeroPosition
                return if (position <= 2) {
                    false
                } else true
            }
        /**
         *
         * @return 大于6返回false
         */
        val isMoveDown: Boolean
            get() {
                val position = zeroPosition
                return if (position >= 6) {
                    false
                } else true
            }
        /**
         *
         * @return 0，3，6返回false
         */
        val isMoveLeft: Boolean
            get() {
                val position = zeroPosition
                return if (position % 3 == 0) {
                    false
                } else true
            }
        /**
         *
         * @return 2，5，8不能右移返回false
         */
        val isMoveRight: Boolean
            get() {
                val position = zeroPosition
                return if (position % 3 == 2) {
                    false
                } else true
            }

        /**
         *
         * @param move 0：上，1：下，2：左，3：右
         * @return 返回移动后的状态
         */
        fun moveUp(move: Int): EightPuzzle {
            val temp = EightPuzzle()
            temp.num = num.clone()
            val position = zeroPosition    //0的位置
            var p = 0                            //与0换位置的位置
            when (move) {
                0 -> {
                    p = position - 3
                    temp.num[position] = num[p]
                }
                1 -> {
                    p = position + 3
                    temp.num[position] = num[p]
                }
                2 -> {
                    p = position - 1
                    temp.num[position] = num[p]
                }
                3 -> {
                    p = position + 1
                    temp.num[position] = num[p]
                }
            }
            temp.num[p] = 0
            return temp
        }

        /**
         * 按照八数码的格式输出
         */
        fun print() {
            for (i in 0..8) {
                if (i % 3 == 2) {
                    println(this.num[i])
                } else {
                    print(this.num[i].toString() + "  ")
                }
            }
        }

        /**
         * 反序列的输出状态
         */
        fun printRoute() {
            var temp: EightPuzzle? = this
            var count = 0
            while (temp != null) {
                temp.print()
                println("----------分割线----------")
                temp = temp.parent
                count++
            }
            println("步骤数：" + (count - 1))
        }

        /**
         *
         * @param open open表
         * @param close close表
         * @param parent 父状态
         * @param target 目标状态
         */
        fun operation(open: ArrayList<EightPuzzle>, close: ArrayList<EightPuzzle>, parent: EightPuzzle, target: EightPuzzle) {
            if (this.isContains(close) == -1) {
                val position = this.isContains(open)
                if (position == -1) {
                    this.parent = parent
                    this.init(target)
                    open.add(this)
                } else {
                    if (this.depth < open[position].depth) {
                        open.removeAt(position)
                        this.parent = parent
                        this.init(target)
                        open.add(this)
                    }
                }
            }
        }

        fun  run(stnum: IntArray, tanum:IntArray){
            //定义open表
            val open = ArrayList<EightPuzzle>()
            val close = ArrayList<EightPuzzle>()
            val start = EightPuzzle()
            val target = EightPuzzle()

            //BufferedReader br = new BufferedReader(new FileReader("./input.txt") );
           // var lineContent: String? = null
            //var order = 0
            /*try {
                val br: BufferedReader
                br = BufferedReader(FileReader("input.txt"))
                while ((lineContent = br.readLine()) != null) {
                    val str = lineContent!!.split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    for (i in str.indices) {
                        if (order == 0)
                            stnum[i] = Integer.parseInt(str[i])
                        else
                            tanum[i] = Integer.parseInt(str[i])
                    }
                    order++
                }
            } catch (e: NumberFormatException) {
                println("请检查输入文件的格式，例如：2,1,6,4,0,8,7,5,3 换行 1,2,3,8,0,4,7,6,5")
                e.printStackTrace()
            } catch (e: IOException) {
                println("当前目录下无input.txt文件。")
                e.printStackTrace()
            }*/

            start.num = stnum
            target.num = tanum
            var count = 0
            val startTime = System.currentTimeMillis()   //获取开始时间
            if (start.isSolvable(target)) {
                //初始化初始状态
                start.init(target)
                open.add(start)
                while (open.isEmpty() == false) {
                    count++;
                    Collections.sort(open)            //按照evaluation的值排序
                    val best = open[0]    //从open表中取出最小估值的状态并移除open表
                    open.removeAt(0)
                    close.add(best)
                    if (best.isTarget(target)) {
                        //输出
                        best.printRoute()
                        val end = System.currentTimeMillis() //获取结束时间
                        println("程序运行时间： " + (end - startTime) + "ms,count=$count")
                        System.exit(0)
                    }
                    var move: Int
                    //由best状态进行扩展并加入到open表中
                    //0的位置上移之后状态不在close和open中设定best为其父状态，并初始化f(n)估值函数
                    if (best.isMoveUp) {
                        move = 0
                        val up = best.moveUp(move)
                        up.operation(open, close, best, target)
                    }
                    //0的位置下移之后状态不在close和open中设定best为其父状态，并初始化f(n)估值函数
                    if (best.isMoveDown) {
                        move = 1
                        val up = best.moveUp(move)
                        up.operation(open, close, best, target)
                    }
                    //0的位置左移之后状态不在close和open中设定best为其父状态，并初始化f(n)估值函数
                    if (best.isMoveLeft) {
                        move = 2
                        val up = best.moveUp(move)
                        up.operation(open, close, best, target)
                    }
                    //0的位置右移之后状态不在close和open中设定best为其父状态，并初始化f(n)估值函数
                    if (best.isMoveRight) {
                        move = 3
                        val up = best.moveUp(move)
                        up.operation(open, close, best, target)
                    }

                }
            } else
                println("没有解，请重新输入。")
        }

    }