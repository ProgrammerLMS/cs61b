package byow.lab12;

import byow.TileEngine.TERenderer;
import byow.TileEngine.TETile;
import byow.TileEngine.Tileset;

import java.util.Random;

/*
 * Draws a world consisting of hexagonal regions.
 */
public class HexWorld {
    private static final int WIDTH = 50;
    private static final int HEIGHT = 50;

    private static final long SEED = 2873123;
    private static final Random RANDOM = new Random(SEED);

    /* You can also think about making a Hexagon class.
       This will require some careful thinking about what a Hexagon object is in this program */

    /* Position相当于是Hexagon，代表了六边形的坐标信息 */
    private static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }

        public Position shift(int dx, int dy) {
            return new Position(this.x + dx, this.y + dy);
        }
    }

    /* 高度的抽象化，绘制六边形的任务，实际上是一行行地绘制，所以抽象出了这个函数 */
    public static void drawRow(TETile[][] tiles, Position p, TETile tile, int length) {
        for (int dx = 0; dx < length; dx++) {
            /* 就挺无语的，和数组意义又不一样，x轴代表列，y轴代表行 */
            tiles[p.x + dx][p.y] = tile;
        }
    }

    /* create a method addHexagon that adds a hexagon of side length s
       to a given position in the world.*/
    public static void addHexagon(TETile[][] tiles, Position p, TETile t, int size) {
        if (size < 2) {
            return;
        }
        addHexagonHelper(tiles, p, t, size - 1, size);
    }

    /*
    * 这个函数实际上就是绘制六边形任务的递归函数，对称的一行行作图
    * 可以说是非常非常的巧妙
    * Position 是一行的起始点（注意不是Tile）
    * b 代表了要往右挪多少个空格
    * t 代表了Tile的数目
    * */
    public static void addHexagonHelper(TETile[][] tiles, Position p, TETile tile, int b,
                                        int t) {
        Position startOfRow = p.shift(b, 0);
        drawRow(tiles, startOfRow, tile, t);

        if (b > 0) {
            Position nextP = p.shift(0, -1);
            /* b-1是因为越往下空格部分越少，t+2是因为下一行比这一行多两个Tile */
            addHexagonHelper(tiles, nextP, tile, b - 1, t + 2);
        }

        Position startOfReflectedRow = startOfRow.shift(0, -(2 * b + 1));
        drawRow(tiles, startOfReflectedRow, tile, t);
    }

    /* 至此，addHexagon可以帮助我们绘制一个六边形了 */

    /* 现在分析绘制大图案的任务，我们可以发现，从上往下一列列绘制，比较简洁
       因此，我们抽象出一个绘制一列六边形的函数 */
    /* num就是我们要绘制的一列数量，其实这里完全可以用for-loop的，TA太喜欢递归了吧 */
    public static void addHexColum(TETile[][] tiles, Position p, int size, int num) {
        if (num < 1) {
            return;
        }

        addHexagon(tiles, p, randomTile(), size);

        if (num > 1) {
            Position bottomNeighbor = getBottomNeighbor(p, size);
            addHexColum(tiles, bottomNeighbor, size, num - 1);
        }
    }

    public static Position getBottomNeighbor(Position p, int n) {
        return p.shift(0, -2 * n);
    }

    public static Position getTopRightNeighbor(Position p, int n) {
        return p.shift(2 * n - 1, n);
    }

    public static Position getBottomRightNeighbor(Position p, int n) {
        return p.shift(2 * n - 1, -n);
    }

    public static void drawWorld(TETile[][] tiles, Position p, int hexSize, int tessSize) {
        addHexColum(tiles, p, hexSize, tessSize);

        for (int i = 1; i < tessSize; i++) {
            p = getTopRightNeighbor(p, hexSize);
            addHexColum(tiles, p, hexSize, tessSize + i);
        }

        for (int i = tessSize - 2; i >= 0; i--) {
            p = getBottomRightNeighbor(p, hexSize);
            addHexColum(tiles, p, hexSize, tessSize + i);
        }
    }


    /*
    * Tip: If you want randomized colors for your hexagon tiles,
    * e.g. so that not every flower is exactly the same,
    * see the TETile.colorVariant method
    * */
    private static TETile randomTile() {
        int tileNum = RANDOM.nextInt(5);
        switch (tileNum) {
            case 0:
                return Tileset.AVATAR;
            case 1:
                return Tileset.WALL;
            case 2:
                return Tileset.FLOOR;
            case 3:
                return Tileset.WATER;
            default:
                return Tileset.GRASS;
        }
    }

    // 对世界进行初始化
    public static void fillBoardWithNothing(TETile[][] tiles) {
        int height = tiles[0].length;
        int width = tiles.length;
        for (int x = 0; x < width; x += 1) {
            for (int y = 0; y < height; y += 1) {
                tiles[x][y] = Tileset.NOTHING;
            }
        }
    }

    public static void main(String[] args) {
        // initialize the tile rendering engine with a window of size WIDTH x HEIGHT
        TERenderer ter = new TERenderer();
        ter.initialize(WIDTH, HEIGHT);

        TETile[][] world = new TETile[WIDTH][HEIGHT];
        fillBoardWithNothing(world);

        /*
        * world[1][5] = Tileset.FLOWER;
          world[5][5] = Tileset.WALL;
        * 非常令人疑惑的，是从左下角开始算作(0, 0)
        * world[1][5]是左下角开始的 第二列，第六行
        * */

        Position anchor = new Position(10, 35);
        drawWorld(world, anchor, 3, 4);

        // draws the world to the screen
        ter.renderFrame(world);

    }
}
