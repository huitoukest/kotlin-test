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
 *
 *   资料:猜想 N×N的棋盘，N为奇数时，与八数码问题相同。逆序奇偶同性可互达
 *   N为偶数时，空格每上下移动一次，奇偶性改变。称空格位置所在的行到目标空格所在的行步数为空格的距离（不计左右距离），若两个状态的可相互到达，则有，两个状态的逆序奇偶性相同且空格距离为偶数，或者，逆序奇偶性不同且空格距离为奇数数。否则不能。
 *   也就是说，当此表达式成立时，两个状态可相互到达：(状态1奇偶性==状态2奇偶性)==(空格距离%2==0)。
 */
fun main(args: Array<String>) {
    val stnum4 = intArrayOf(2, 1, 6, 4, 0, 8, 7, 5, 3 , 13 , 15, 10 ,11 ,14 ,9 ,12 )//中等难度->70步,88328ms,count=172792
    //var stnum4 = intArrayOf(1,4,7,5,9,3,0,10,6,8,2,12,11,13,15,14);//中等难度51步奏,count=7349

    val tanum4 = intArrayOf(1, 4, 7, 2, 0, 6, 5, 3 ,12 ,13,15, 8 , 9 ,14, 10 ,11)//最高难度,72-194步 程序运行时间： 290086ms,count=282048
    //val tanum4 = intArrayOf(1,6,7,4,2,8,5,10,3,13,9,15,0,11,14,12)//最简单难度->10步奏
    //val  tanum4 = intArrayOf(7,12 ,13, 2, 6, 0, 3, 5 ,15, 8 , 9 ,14, 1, 4,  10 ,11)//最高难度步骤数：没算出来

    val stnum3 = intArrayOf(2, 1, 6, 4, 0, 8, 7, 5, 3 )
    val tanum3 = intArrayOf(1, 4, 7, 2, 0, 6, 5, 3, 8 )

    //EightPuzzlePre(3,stnum3).runByStar(tanum3)
    //EightPuzzlePre(4,stnum4).runByStar(tanum4)
    //EightPuzzlePre(3,stnum3).runByStarBinay(tanum3)
    EightPuzzlePre(4,stnum4).runByStarBinay(tanum4)
}

//定义移动的四个方向
enum class Direction {
    UP, DOWN, LEFT, RIGHT,CENTER
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

//边界索引
class BoundryArray(length:Int){
    var left = IntArray(length)
    var right = IntArray(length)
    var up = IntArray(length)
    var down = IntArray(length)
}

//根据一个维度,依次返回其上下左右四个边的坐标,是一个List包含四个Array
class Boundrys(dimension:Int){
    val dim = dimension

    //从0到最外层依次是其边界
    //比如4*4的矩阵,index=0中存放的是其外围边框.index=1存放的是其去掉外围边框后剩下的边框.
    fun getBoundrys(dimension:Int):ArrayList<BoundryArray>{
        var boundrys = ArrayList<BoundryArray>(dimension)
        var size = dimension * dimension
        var boundryLevel = dimension / 2 //最多有 boundryLevel层次的边界信息
        for(i in  0 .. size -1){
            var y = i / dimension
            var x = i % dimension
            var xtmp = if(x >= boundryLevel ) dimension - x -1 else x
            var ytmp = if(y >= boundryLevel ) dimension - y -1 else y
            var min = Math.min(xtmp,ytmp)
            if(boundrys.size <= min){
                boundrys.add(BoundryArray(dimension - min * 2))
            }
            var bArray = boundrys.get(min)
            if(y == min){
                bArray.up[x - min] = i
            }
            if(y == (dimension - min -1)){
                bArray.down[x - min ] = i
            }
            if(x == min){
                bArray.left[y - min] = i
            }
            if(x == (dimension - min -1)){
                bArray.right[y - min] = i
            }
        }
        return boundrys
    }
}

class InsertSort {
    //binarySearch()方法的返回值为：
    // 1、如果找到关键字，则返回值为关键字在数组中的位置索引，且索引从0开始
    // 2、如果没有找到关键字，返回值为负的插入点值，所谓插入点值就是第一个比关键字大的元素在数组中的位置索引，而且这个位置索引从1开始。
    /**
     * 默认升序插入,插入前需要保持有序
     * 将插入的数据"二次散列化"
     */
    fun insertSort(list:ArrayList<EightPuzzlePre> , pre:EightPuzzlePre , maxSize:Int = 10000000) {
        if (list == null ) {
            return
        }
        if(list.size < 2){
            list.add(pre)
        }else if(list.size > maxSize){
            list.removeAt(maxSize)
        }

        var index = Collections.binarySearch(list,pre)
        if( index > -1){//若数组中存在
            //list.add()
            list.add(index,pre)
        }else{//若数组中不存在返回的是负数的插入值
            index = Math.abs(index) - 1
            list.add(index,pre)
        }
    }

    /**
     * 存在返回 0和正整数
     * 不存在返回 -1
     */
    fun brySearch(list:ArrayList<EightPuzzlePre> , pre:EightPuzzlePre):Int{
        var index = Collections.binarySearch(list,pre)
        if (index < 0 ){
            index = -1
        }else{
             var isExsited = false
             for(i in index..list.size-1 ){//依次搜索
                 if(list.get(i).evaluation > pre.evaluation){
                     break
                 }
                 if(pre.equals(list.get(i))){
                     isExsited = true
                     index = i
                     break
                 }
             }
            if(!isExsited){
                for(i in index downTo 0){
                    if(list.get(i).evaluation < pre.evaluation){
                        break
                    }
                    if(pre.equals(list.get(i))){
                        isExsited = true
                        index = i
                        break
                    }
                }
            }
            if(!isExsited){
                index = -1
            }
        }
        return  index
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
            if(num.size != size)
            {
                throw RuntimeException("num size error")
            }
        }
        companion object {
            private var moveDirectionMap:MutableMap<Int,ArrayList<Direction>>? = null //用来记录每一个步骤
            private var evaluteStep:Array<IntArray>? = null;
            private var boundarys:ArrayList<BoundryArray>? = null
            val insertUtil = InsertSort()

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
                        for(i in rng){
                            val ary = evaluteStep!![i]
                            for(j in rng){
                                temp = 0
                                var  ii = i + 1
                                var  jj = j + 1
                                var upDownValue = Math.abs(ii / dimension - jj/dimension) //计算纵向距离
                                temp +=  upDownValue
                                temp += Math.abs(i % dimension - j % dimension) //计算横向距离
                                ary[j] = temp
                            }
                        }
                    }

                    if(null == boundarys){
                        boundarys = Boundrys(dimension).getBoundrys(dimension)
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
            //返回边界位置
            fun getBoundaryIndex(dimension:Int):ArrayList<BoundryArray>{
                init(dimension)
                return boundarys!!
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

        fun getEvl01(target: EightPuzzlePre):Int{
            var temp = 0
                for (i in range) {//将不相等的个数作为估价值
                if (exNum.num[i] != target.exNum.num[i])
                    temp += 2
            }
            return temp
        }

        fun getEvl011(target: EightPuzzlePre):Int{
            var temp = 0
            var left = intArrayOf(0,4,8,12,7,11,1,2,3,13,14,15)
            for (i in range) {//将不相等的个数作为估价值
                if (exNum.num[i] != target.exNum.num[i])
                    if(left.contains(i)) {
                        temp += 3
                    }else{
                        temp += 1
                    }

            }
            return temp
        }

        /**
         * 将相同值得位置移动的步数差作为估价值
         */
        fun getEvl02(target: EightPuzzlePre):Int{
                var temp = 0
                for(i in range){
                    if(exNum.num[i] == target.exNum.num[i]){//相同位置的两个数相等
                        continue
                    }/*else{
                         temp += 0
                    }*/
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
                return temp
        }

        //如果父结果集中已经有了某一侧的有序,那么当前移动方向导致其无序的,则直接代价为最大值
        //相当于使之成为不可达区域
        fun getEvl03(target: EightPuzzlePre):Int{
            var temp = 0
            var left = intArrayOf(0,4,8,12)
            var right = intArrayOf(3,7,11,15)
            var up = intArrayOf(0,1,2,3)
            var down = intArrayOf(12,13,14,15)

            fun isEqBoudry(indexs:IntArray,cur:EightPuzzlePre,tar:EightPuzzlePre):Boolean{
                var isEq = true
                for(index in 0 .. indexs.size -1){
                    var it = indexs[index]
                    if( cur.exNum.num[it] != tar.exNum.num[it])
                    {
                        isEq = false
                        break
                    }

                }
                return isEq
            }
            var errorAdd = 2
            if(this.parent != null) {
                if (isEqBoudry(left, this.parent!!, target) && left.contains(zeroPosition) && !left.contains(this.parent!!.zeroPosition)) {
                    temp += temp + errorAdd
                }
                if (isEqBoudry(right, this.parent!!, target) && right.contains(zeroPosition) && !right.contains(this.parent!!.zeroPosition)) {
                    temp += temp + errorAdd
                }
                if (isEqBoudry(up, this.parent!!, target) && up.contains(zeroPosition) && !up.contains(this.parent!!.zeroPosition)) {
                    temp += temp + errorAdd
                }
                if (isEqBoudry(down, this.parent!!, target) && down.contains(zeroPosition) && !down.contains(this.parent!!.zeroPosition)) {
                    temp += temp + errorAdd
                }
            }
                temp += getEvl02(target) * 3 + getEvl011(target)
            return temp
        }

    /**
     * 边界不等,权值+ n
     * 依次向中心过渡.边界连等区域额外加x
     *
     */
    fun getEvl04(target: EightPuzzlePre):Int{
        var boundrys = getBoundaryIndex(dimension)
        var temp = 0
        fun getBoudryAdd(indexs:IntArray,cur:EightPuzzlePre,tar:EightPuzzlePre,addHvOne:Int,addAllValue:Int):Int{
            var addValue = 0
            var eqCount = 0
            for(index in 0 .. indexs.size -1){
                var it = indexs[index]
                //if(cur.exNum.num[it] == 0){
                   // eqCount ++
                    //continue
                //}
                if( cur.exNum.num[it] != tar.exNum.num[it])
                {
                    addValue += addHvOne
                }else{
                    eqCount ++
                }
            }
            if(eqCount < indexs.size){
                addValue += addAllValue
            }
            return addValue
        }

        for(i in 0 .. boundrys.size -1){
            var boundr = boundrys.get(i)
            var addOne =  boundrys.size - i
            var addAll = (dimension - i) * (dimension - i)
            //判断4个边界,其中四角区域需要重复
            for (i in 0 .. boundr.up.size - 1) {//将不相等的个数作为估价值
                temp += getBoudryAdd(boundr.up,this,target,addOne,addAll)
            }
            for (i in 0 .. boundr.down.size - 1) {
                temp += getBoudryAdd(boundr.down,this,target,addOne,addAll)
            }
            for (i in 0 .. boundr.left.size - 1) {
                temp += getBoudryAdd(boundr.left,this,target,addOne,addAll)
            }
            for (i in 0 .. boundr.right.size - 1) {
                temp += getBoudryAdd(boundr.right,this,target,addOne,addAll)
            }
        }
        temp += getEvl02(target) * 2
        return temp
    }

        fun initEvaluation(target:EightPuzzlePre):Int{
            //var temp = getEvl02(target) + getEvl01(target)
            //var temp = Math.max(getEvl02(target),getEvl01(target))
            var temp = getEvl04(target)
            this.misposition = temp
            if (this.parent == null) {
                this.depth = 0
            } else {
                this.depth = this.parent!!.depth + 1
            }
            this.evaluation = this.depth  + this.misposition //这里是将步数,搜索深度信息作为gn(预估的实际代价)

            //将评估数据的数据"二次散列化",这样在插入排序和搜索的时候速度更快
            var maxHashValue = 1000000
            evaluation = evaluation * maxHashValue + this.exNum.hashCode() % maxHashValue
            return evaluation
        }
        /**
         * 求f(n) = g(n)+h(n);
         * 初始化状态信息
         * @param target
         */
        fun init(target: EightPuzzlePre) {
            initEvaluation(target)
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
         * 当M(一行有M列)为奇数时，两种数码可以互达与两种数码【排列逆序对数（不算0）】奇偶性相同等价。
         * 当M为偶数时，两种数码可以互达与两种数码【排列逆序对数（不算0）+0的纵坐标】奇偶性相同等价。
         * 求逆序值并判断是否有解
         * @param target
         * @return 有解：true 无解：false
         */
        fun isSolvable(target: EightPuzzlePre): Boolean {
            if(this.exNum.num.size != target.exNum.num.size){
                return false
            }
            if(exNum.num.sum() != exNum.num.sum()){
                return false
            }
            var reverse1 = 0
            var reverse2 = 0
            for (i in range) {//num[i]是后面的数
                for (j in 0..i - 1) {//num[j]是前面的数
                    /*if(i == zeroPosition || j == target.zeroPosition){
                        continue
                    }*/
                    if (exNum.num[j] > exNum.num[i])
                        reverse1++
                    if (target.exNum.num[j] > target.exNum.num[i])
                        reverse2++
                }
            }
            var isSolvable = false //偶数时,上下移动改变逆序数的奇偶性,N为偶数时，逆序数之和sum加上空格所在行距目标空格行的距离dis之和要和终点状态逆序数同奇偶
            if(this.dimension % 2 == 0 && (reverse1 + reverse2 + this.zeroPosition / dimension + target.zeroPosition / dimension) % 2 == 0 ){
                isSolvable = true
            }else if(this.dimension % 2 == 1 && (reverse1 + reverse2) % 2 ==0){
                isSolvable = true
            }
            return isSolvable
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

        fun printList() {
            for (i in range) {
                if(i>0){
                    print(",")
                }
                print(this.exNum.num[i])

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


    fun  runByStarBinay(tanum:IntArray){
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
                nextSteps.add(current)
            }
            if(!nextSteps.isEmpty()){
                var next = nextSteps.get(0)
                if(count % 10000 == 0 && start.isSolvable(next)){
                    println("evalute:${next.evaluation},nextStepsSize:${nextSteps.size},中间值：")
                    next.printList()
                    println()
                }
                if (next.isTarget(target)) {
                    //输出
                    next.printRoute()
                    val end = System.currentTimeMillis() //获取结束时间
                    println("程序运行时间： " + (end - startTime) + "ms,count=$count")
                    System.exit(0)
                }
                insertUtil.insertSort(history,next) //history.add(next)
                nextSteps.remove(next)
            }else{
                println("misscount")
                System.exit(0)
            }
            var directions = EightPuzzlePre.getCanMoveDirections(dimension,current.zeroPosition)
            directions.forEach {
                val next = current.moveUp(it,dimension) //按照给定方向移动一步之后,由best状态进行扩展并加入到open表中
                next.parent = current
                next.init(target)
                val historyPosition = insertUtil.brySearch(history,next)//next.isContains(history),通过评估值折半搜索
                if (historyPosition == -1) {//判重,如果原来没走过
                    var nextPosition = insertUtil.brySearch(nextSteps,next)//next.isContains(nextSteps)
                    if(nextPosition == -1){//如果没有预判
                        insertUtil.insertSort(nextSteps, next)//nextSteps.add(next)
                    }else{//如果已经预判评估过
                        if(next.depth < nextSteps.get(nextPosition).depth){
                            nextSteps.removeAt(nextPosition)
                            insertUtil.insertSort(nextSteps,next)
                        }
                    }
                }/*else if(next.depth < history.get(historyPosition).depth){//虽然重复,但是找到了更短的实现方式时
                        nextSteps.add(next)
                        history.removeAt(historyPosition)
                        next.init(target)
                    }*/
            }
            count ++ ;
            if(count % 10000 == 0) println("已经运行count=$count 步")
            runSearch(nextSteps.get(0),nextSteps,history)
        }

        if (start.isSolvable(target)) {
            runSearch(start,nextSteps,history)
        }else{
            println("没有解，请重新输入。")
        }
        println("misscount is $misCount")
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EightPuzzlePre

        if (exNum != other.exNum) return false

        return true
    }

    override fun hashCode(): Int {
        return exNum.hashCode()
    }

}