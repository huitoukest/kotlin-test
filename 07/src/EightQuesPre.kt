import com.sun.javafx.binding.StringFormatter
import java.util.*
import kotlin.collections.ArrayList

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
    val stnum4 = intArrayOf(2, 1, 6, 4, 0, 8, 7, 5, 3 ,9 ,10 ,11 ,12 ,13 ,14 ,15 )
    val tanum4 = intArrayOf(1, 2, 3, 8, 0, 4, 7, 6, 5 ,15, 10 ,11 ,9 ,13  ,14 ,12)

    val stnum3 = intArrayOf(2, 1, 6, 4, 0, 8, 7, 5, 3 )
    val tanum3 = intArrayOf(1, 4, 7, 2, 0, 6, 5, 3, 8 )

    EightPuzzlePre(3,stnum3).runByStar(tanum3)
    //EightPuzzlePre(4,stnum4).runByStar(tanum4)
}

//定义移动的四个方向
enum class Direction {
    UP, DOWN, LEFT, RIGHT
}

class ExtIntArray(num:IntArray){
    var num = num
    var size2 = num.size * num.size
    var maxHashValue = Int.MAX_VALUE  - size2
    var hash = 1

    //每次修改完值之后,初始化hash值
    fun  initHash(){
         hash = 0
         for(i in num){
             if(hash < maxHashValue){
                 hash = hash * 10 + i
             }else{
                 hash = hash + i
             }
         }
        hash = hash * 10 + num.size
    }

    override fun equals(other: Any?): Boolean {
        if(null == other || other !is ExtIntArray){
            return false
        }
        if(this.hashCode() != other.hashCode()){
            return false
        }
        return Arrays.equals(this.num,other.num)
    }

    override fun hashCode(): Int {
        return hash
    }

    fun isContains(open: ArrayList<EightPuzzlePre>): Int {
        for (i in open.indices) {
            if ((open[i].exNum.equals(this))) {
                return i
            }
        }
        return -1
    }

    fun clone(): ExtIntArray {
        var tarNum = this.num.clone();
        var tar =  ExtIntArray(tarNum);
        tar.initHash()
        return tar
    }


}

/**
 * A*算法解决八数码问题 Kotlin语言实现
 * @parm dimension 维度信息,即dimension X dimension的矩阵
 */

/**
 * 优化方向:
 * 1.优化从当前状态到目标状态的估价函数.通过求所有点x点->y点的横纵变化之和来得到比较精确的估价值
 * 2.位置对应的可以移动的关系Map,不用每次都输入位置来查找可以移动的方向
 * 3.通过优化组数的相等性比较,给数组生成一个hash码,当码不同时表示不等,hash码相同时在具体对比数组值
 * 4.优化排序,因为排序实际上是有序的,所以每次插入新的值时应该采用插入排序的方式来实现
 *
 */
class EightPuzzlePre(dimension:Int,num:IntArray) : Comparable<Any> {
        var dimension = dimension
        var size = dimension * dimension //当前位置个数
        var exNum = ExtIntArray(num)  //当前的盘面状态
        var depth: Int = 0                    //当前的深度即走到当前状态的步骤
        var evaluation: Int = 0               //从起始状态到目标的最小估计值
        var misposition: Int = 0              //到目标的最小估计
        var parent: EightPuzzlePre? = null      //当前状态的父状态
        var range = 0..size - 1
        /**
         * @return 返回0在八数码中的位置
         */
        var zeroPosition: Int = -1
        init {
            if(zeroPosition == -1){//初始化0的位置
                for (i in range) {
                    if (this.exNum.num[i] == 0) {
                        zeroPosition = i
                    }
                }
            }
            exNum.initHash()
        }
        companion object {
            private var moveDirectionMap:MutableMap<Int,ArrayList<Direction>>? = null //用来记录每一个步骤
            private var evaluteStep:Array<IntArray>? = null;


            /**
             * @param zeroPosition 位置从0开始zeroPosition
             *
             */
            fun getEvaluteStep(dimension: Int,currentPosition:Int,targetPosition:Int):Int{
                init(dimension);
                return evaluteStep!![currentPosition][targetPosition];
            }

            @Synchronized fun init(dimension: Int):Unit{
                    if(null == moveDirectionMap) {
                        moveDirectionMap = mutableMapOf()
                        var size = dimension * dimension
                        for (position in 0..dimension * dimension - 1) {
                            var list = ArrayList<Direction>(4)
                            var positionTmp = (position + 1)
                            //依次判断四个移动方向

                            if (positionTmp % dimension != 0) {//可以右移动
                                list.add(Direction.RIGHT)
                            }
                            if (positionTmp % dimension != 1) {//可以左移动
                                list.add(Direction.LEFT)
                            }
                            if (positionTmp  > dimension) {//可以上移动
                                list.add(Direction.UP)
                            }
                            if (positionTmp <= (size  - dimension)) {//可以下移动
                                list.add(Direction.DOWN)
                            }
                            moveDirectionMap!![position] = list;
                        }
                    }
                    if(null == evaluteStep){//求出表格内不同两个位置之间需要移动的步数,索引/下标值最低是0
                        val size = dimension * dimension

                        evaluteStep  = Array<IntArray>(size,{it -> IntArray(size,{it->0})})
                        val rng = 0 .. size - 1
                        var temp = 0
                        var tmpHv = 0
                        for(i in rng){
                            val ary = evaluteStep!![i]
                            for(j in rng){
                                temp = 0
                                tmpHv = Math.abs(i - j);//如果在同一行
                                if( tmpHv >= dimension) {//如果不在同一行
                                    var  ii = i + 1
                                    var  jj = j + 1
                                    temp += Math.abs(ii / dimension - jj/dimension)  //计算纵向距离
                                    temp += Math.abs(i % dimension - j % dimension) //计算横向距离
                                }else{
                                    temp = tmpHv
                                }
                                ary[j] = temp
                            }
                        }
                    }
            }

            /**
             * @param zeroPosition 位置从0开始zeroPosition
             *
             */
            fun getCanMoveDirections(dimension:Int,zeroPosition:Int):ArrayList<Direction>{
                init(dimension);
                return moveDirectionMap!!.get(zeroPosition)?:ArrayList<Direction>();
            }
        }


        /**
         * 判断当前状态是否为目标状态
         * @param target
         * @return
         */
        fun isTarget(target: EightPuzzlePre): Boolean {
            //return //Arrays.equals(num, target.num)
            return       this.exNum.equals(target.exNum)
        }

        /**
         * 求f(n) = g(n)+h(n);
         * 初始化状态信息
         * @param target
         */
        fun init(target: EightPuzzlePre) {
            var temp = 0
           /* for (i in range ) {
                if (num[i] != target.num[i])
                    temp++   //预估当前位置到目标位置变幻的成本值(h(n))
            }*/

            for(i in range){
                if(exNum.num[i] == target.exNum.num[i]){//相同位置的两个数相等
                    continue
                }else{
                    temp += 1
                }
                for(j in range){
                    if(exNum.num[j] == target.exNum.num[j]){//相同位置的两个数相等
                        continue
                    }
                    if(exNum.num[i] == target.exNum.num[j]){
                        temp += getEvaluteStep(dimension,i,j)
                        break
                    }
                }
            }
            this.misposition = temp
            if (this.parent == null) {
                this.depth = 0
            } else {
                this.depth = this.parent!!.depth + 1
            }
            this.evaluation = this.depth + this.misposition //这里是将步数,搜索深度信息作为gn(预估的实际代价)

            if(zeroPosition == -1){//初始化0的位置
                 for (i in range) {
                    if (this.exNum.num[i] == 0) {
                        zeroPosition = i
                    }
                }
            }
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
        fun isSolvable(target: EightPuzzlePre): Boolean {
            var reverse = 0
            for (i in range) {//num[i]是后面的数
                for (j in 0..i - 1) {//num[j]是前面的数
                    if (exNum.num[j] > exNum.num[i])
                        reverse++
                    if (target.exNum.num[j] > target.exNum.num[i])
                        reverse++
                }
            }
            return if (reverse % 2 == 0) true else false
        }

        override operator fun compareTo(other: Any): Int {
            if (other is EightPuzzlePre){
                return this.evaluation - other.evaluation//默认排序为f(n)由小到大排序
            }
            return -1;
        }

        /**
         *
         * @param open    状态集合
         * @return 判断当前状态是否存在于open表中
         */
        fun isContains(open: ArrayList<EightPuzzlePre>): Int {
            for (i in open.indices) {
                if (open[i].exNum.equals(this.exNum)) {
                    return i
                }
            }
            return -1
        }

        /**
         *
         * @param move 0：上，1：下，2：左，3：右
         * @return 返回移动后的状态
         */
        fun moveUp(direction:Direction,dimension:Int): EightPuzzlePre {
            val temp = EightPuzzlePre(dimension,exNum.num)
            temp.exNum = exNum.clone()
            val position = zeroPosition    //0的位置
            var p = 0                            //与0换位置的位置
            when (direction) {
                Direction.UP -> {
                    p = position - dimension
                    temp.exNum.num[position] = exNum.num[p]
                }
                Direction.DOWN -> {
                    p = position + dimension
                    temp.exNum.num[position] = exNum.num[p]
                }
                Direction.LEFT -> {
                    p = position - 1
                    temp.exNum.num[position] = exNum.num[p]
                }
                Direction.RIGHT -> {
                    p = position + 1
                    temp.exNum.num[position] = exNum.num[p]
                }
            }
            temp.exNum.num[p] = 0
            temp.zeroPosition = p
            temp.exNum.initHash()
            return temp
        }

        /**
         * 按照八数码的格式输出
         */
        fun print() {
            for (i in range) {
                if (i % dimension == dimension - 1 ) {
                    println(String.format("%-4d",this.exNum.num[i]))
                } else {
                    print(String.format("%-4d",this.exNum.num[i]))
                }
            }
        }

        /**
         * 反序列的输出状态
         */
        fun printRoute() {
            var temp: EightPuzzlePre? = this
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
        fun operation(open: ArrayList<EightPuzzlePre>, close: ArrayList<EightPuzzlePre>, parent: EightPuzzlePre, target: EightPuzzlePre) {
            if (this.isContains(close) == -1) {//判重,如果原来没走过
                val position = this.isContains(open)
                if (position == -1) {
                    this.parent = parent
                    this.init(target)
                    open.add(this)
                } else {//如果已经经过(评估和生成),则取步奏最少的作为父步奏
                    if (this.depth < open[position].depth) {
                        open.removeAt(position)//同时从评估中移出此步奏,去重
                        this.parent = parent
                        this.init(target)
                        open.add(this)
                    }
                }
            }
        }

        fun  run(tanum:IntArray){//OPEN表保存所有已生成而未考察的节点，CLOSED表中记录已访问过的节点。
            //定义open表
            val open = ArrayList<EightPuzzlePre>()  //记录临时的下一步数列状态,是所有当前走过的状态的下一步的评估值.
            val close = ArrayList<EightPuzzlePre>() //记录走过的数列状态,是真实走过的记录
            val start = EightPuzzlePre(dimension,exNum.num)
            val target = EightPuzzlePre(dimension,tanum)
            var count = 0
            val startTime = System.currentTimeMillis()   //获取开始时间
            if (start.isSolvable(target)) {
                //初始化初始状态
                start.init(target)
                open.add(start)
                while (open.isEmpty() == false) {
                    count ++ ;
                    Collections.sort(open)            //按照evaluation的值排序
                    val best = open[0]    //从open表中取出最小估值的状态并移除open表
                    open.removeAt(0)
                    close.add(best) //加入当最小值到已经走过的数据中
                    if (best.isTarget(target)) {
                        //输出
                        best.printRoute()
                        val end = System.currentTimeMillis() //获取结束时间
                        println("程序运行时间： " + (end - startTime) + "ms,count=$count")
                        System.exit(0)
                    }
                    if(count % 10000 == 0) println("已经运行count=$count 步")
                    var directions = EightPuzzlePre.getCanMoveDirections(dimension,best.zeroPosition)
                    directions.forEach {
                        val next = best.moveUp(it,dimension) //按照给定方向移动一步之后,由best状态进行扩展并加入到open表中
                        next.operation(open, close, best, target)//0的位置上移之后状态不在close和open中设定best为其父状态，并初始化f(n)估值函数
                    }
                }
            } else
                println("没有解，请重新输入。")
        }

       fun  runByStar(tanum:IntArray){
            //定义open表
            val nextSteps = ArrayList<EightPuzzlePre>(dimension * 10000)  //记录临时的下一步数列状态
            val history = ArrayList<EightPuzzlePre>(dimension * 10000) //记录走过的数列状态
            val start = EightPuzzlePre(dimension,exNum.num)
            val target = EightPuzzlePre(dimension,tanum)
                start.init(target)
            var count = 0
            val startTime = System.currentTimeMillis()   //获取开始时间
            var misCount = 0

           tailrec  fun runSearch(current:EightPuzzlePre,nextSteps:ArrayList<EightPuzzlePre>,history:ArrayList<EightPuzzlePre>){
                if(nextSteps.isEmpty() && history.isEmpty()){
                    nextSteps.add(current);
                }
                if(!nextSteps.isEmpty()){
                    var next = nextSteps.get(0)
                    if (next.isTarget(target)) {
                        //输出
                        next.printRoute()
                        val end = System.currentTimeMillis() //获取结束时间
                        println("程序运行时间： " + (end - startTime) + "ms,count=$count")
                        System.exit(0)
                    }
                    history.add(nextSteps.get(0))
                    nextSteps.remove(next)
                }else{
                    println("misscount")
                    System.exit(0)
                }
                var directions = EightPuzzlePre.getCanMoveDirections(dimension,current.zeroPosition)
                var hasNew = false;
                directions.forEach {
                    val next = current.moveUp(it,dimension) //按照给定方向移动一步之后,由best状态进行扩展并加入到open表中
                    next.parent = current
                    val s = System.currentTimeMillis()
                    val historyPosition = next.isContains(history)
                    if (historyPosition == -1) {//判重,如果原来没走过
                        var nextPosition = next.isContains(nextSteps)
                        if(nextPosition == -1){//如果没有预判
                            next.init(target)
                            nextSteps.add(next)
                            hasNew = true
                        }else{//如果已经预判评估过
                            if(next.depth < nextSteps.get(nextPosition).depth){
                                next.init(target)
                                nextSteps.removeAt(nextPosition)
                                nextSteps.add(next)
                            }
                        }
                    }/*else if(next.depth < history.get(historyPosition).depth){//虽然重复,但是找到了更短的实现方式时
                        nextSteps.add(next)
                        history.removeAt(historyPosition)
                        next.init(target)
                        hasNew = true
                    }*/
                    val e = System.currentTimeMillis()
                    if(count % 2000 == 0 ) println("评估下一步花费时间:" + (e - s))
                }
                if(hasNew){
                    Collections.sort(nextSteps)
                }

               count ++ ;
               if(count % 5000 == 0) println("已经运行count=$count 步")
                runSearch(nextSteps.get(0),nextSteps,history)
            }

            if (start.isSolvable(target)) {
                runSearch(start,nextSteps,history)
            }else{
                println("没有解，请重新输入。")
            }
            println("misscount is $misCount")
        }

 }