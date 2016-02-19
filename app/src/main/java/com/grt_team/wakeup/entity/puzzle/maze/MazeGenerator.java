
package com.grt_team.wakeup.entity.puzzle.maze;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

public class MazeGenerator {

    private MazeGenerator() {
    }

    /**
     * Add wall to collection. Do not check if index is out of bounds.
     * 
     * @param walls
     * @param maze
     * @param point
     */
    private static void addWalls(List<MazePoint> walls, int[][] maze, MazePoint point) {
        if (maze[point.row][point.col - 1] == MazePuzzle.WALL) {
            walls.add(new MazePoint(point.row, point.col - 1));
        }
        if (maze[point.row][point.col + 1] == MazePuzzle.WALL) {
            walls.add(new MazePoint(point.row, point.col + 1));
        }
        if (maze[point.row - 1][point.col] == MazePuzzle.WALL) {
            walls.add(new MazePoint(point.row - 1, point.col));
        }
        if (maze[point.row + 1][point.col] == MazePuzzle.WALL) {
            walls.add(new MazePoint(point.row + 1, point.col));
        }
    }

    @Deprecated
    public static int[][] generatePrimsMaze(int rows, int cols) {
        Random rnd = new Random();

        // Only odd rows and columns available
        rows = rows / 2 * 2 + 1;
        cols = cols / 2 * 2 + 1;

        if (rows < 3) {
            rows = 3;
        }
        if (cols < 3) {
            cols = 3;
        }

        int[][] maze = new int[rows][cols];

        // skip first row as border
        int startRow = 1;
        int startCol = 1 + rnd.nextInt(cols - 2) / 2 * 2;

        // Fill grid with free cells
        for (int i = 1; i < maze.length; i += 2) {
            for (int j = 1; j < maze[i].length; j += 2) {
                maze[i][j] = MazePuzzle.END;
            }
        }
        maze[startRow][startCol] = MazePuzzle.FREE;

        MazePoint wall;
        List<MazePoint> walls = new ArrayList<MazePoint>();
        addWalls(walls, maze, new MazePoint(startRow, startCol));

        while (!walls.isEmpty()) {
            wall = walls.get(rnd.nextInt(walls.size()));

            if (wall.row == 0
                    || wall.row == maze.length - 1
                    || wall.col == 0
                    || wall.col == maze[0].length - 1) {
                walls.remove(wall);
                continue;
            }

            if (maze[wall.row + 1][wall.col] == MazePuzzle.END) {

                maze[wall.row][wall.col] = MazePuzzle.FREE;
                maze[wall.row + 1][wall.col] = MazePuzzle.FREE;
                addWalls(walls, maze, new MazePoint(wall.row + 1, wall.col));

            } else if (maze[wall.row - 1][wall.col] == MazePuzzle.END) {

                maze[wall.row][wall.col] = MazePuzzle.FREE;
                maze[wall.row - 1][wall.col] = MazePuzzle.FREE;
                addWalls(walls, maze, new MazePoint(wall.row - 1, wall.col));

            } else if (maze[wall.row][wall.col + 1] == MazePuzzle.END) {

                maze[wall.row][wall.col] = MazePuzzle.FREE;
                maze[wall.row][wall.col + 1] = MazePuzzle.FREE;
                addWalls(walls, maze, new MazePoint(wall.row, wall.col + 1));

            } else if (maze[wall.row][wall.col - 1] == MazePuzzle.END) {

                maze[wall.row][wall.col] = MazePuzzle.FREE;
                maze[wall.row][wall.col - 1] = MazePuzzle.FREE;
                addWalls(walls, maze, new MazePoint(wall.row, wall.col - 1));

            } else {
                walls.remove(wall);
            }

        }

        maze[1][maze[0].length - 2] = MazePuzzle.END;
        maze[maze.length - 2][maze[0].length - 2] = MazePuzzle.VISITED;

        return maze;
    }

    /**
     * Generate maze with specified number of rows and cols. Maze will be
     * surrounded with walls. End of maze is at the top border and start is the
     * longest path. If there is low start then bottom wall will be broken and
     * start will be there.
     * 
     * @param rows - number of rows, will be round up to odd number
     * @param cols - number of cols, will be round up to odd number
     * @param lowStart - true if bottom wall need to be broken for start cell.
     *            Otherwise start position may be anywhere, where is the longest
     *            path.
     * @return
     */
    public static int[][] generateMaze(int rows, int cols, boolean lowStart) {
        Random rnd = new Random();

        // Only odd rows and columns available
        rows = rows / 2 * 2 + 1;
        cols = cols / 2 * 2 + 1;

        if (rows < 3) {
            rows = 3;
        }
        if (cols < 3) {
            cols = 3;
        }

        int[][] maze = new int[rows][cols];

        // skip first row as border
        int startRow = 1;
        int startCol = 1 + rnd.nextInt(cols - 2) / 2 * 2;

        // set start point for makePath method
        maze[0][startCol] = MazePuzzle.FREE;

        MazePoint start = new MazePoint(startRow, startCol);
        MazePoint longest = new MazePoint(startRow, startCol);

        // create maze from start point
        maze = makePath(maze, start, longest, rnd, lowStart);

        // path is built so mark start point as end of maze
        maze[0][startCol] = MazePuzzle.END;
        if (!lowStart) {
            maze[longest.getRow()][longest.getCol()] = MazePuzzle.VISITED;
        } else {
            // it is low start so break the bottom wall
            maze[longest.getRow() + 1][longest.getCol()] = MazePuzzle.VISITED;
        }
        return maze;
    }

    private static class MazePoint {
        private int row;
        private int col;

        public MazePoint(int row, int col) {
            this.row = row;
            this.col = col;
        }

        public int getRow() {
            return row;
        }

        public int getCol() {
            return col;
        }

        public void setMazePoint(MazePoint p) {
            this.row = p.row;
            this.col = p.col;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + col;
            result = prime * result + row;
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MazePoint other = (MazePoint) obj;
            if (col != other.col)
                return false;
            if (row != other.row)
                return false;
            return true;
        }

    }

    /**
     * Create maze path starting from start point and calculating longest path.
     * If low start then longest path will be calculated to bottom row otherwise
     * longest path may be anywhere.
     * 
     * @param maze
     * @param start
     * @param longest
     * @param rnd
     * @param lowStart
     * @return
     */
    private static int[][] makePath(int[][] maze, MazePoint start, MazePoint longest,
            Random rnd, boolean lowStart) {
        ArrayList<MazePoint> notVisited = new ArrayList<MazePoint>();
        int nextIndex;
        int currentLength = 1;
        int maxLength = 1;

        Stack<MazePoint> points = new Stack<MazeGenerator.MazePoint>();
        points.push(new MazePoint(start.getRow(), start.getCol()));

        MazePoint current;
        while (!points.isEmpty()) {
            current = points.pop();
            maze[current.getRow()][current.getCol()] = MazePuzzle.FREE;

            // get not visited neighbors
            notVisited.clear();
            if (current.getRow() + 2 < maze.length
                    && maze[current.getRow() + 2][current.getCol()] != MazePuzzle.FREE) {
                notVisited.add(new MazePoint(current.getRow() + 2, current.getCol()));
            }
            if (current.getRow() - 2 > -1
                    && maze[current.getRow() - 2][current.getCol()] != MazePuzzle.FREE) {
                notVisited.add(new MazePoint(current.getRow() - 2, current.getCol()));
            }
            if (current.getCol() + 2 < maze[current.getRow()].length
                    && maze[current.getRow()][current.getCol() + 2] != MazePuzzle.FREE) {
                notVisited.add(new MazePoint(current.getRow(), current.getCol() + 2));
            }
            if (current.getCol() - 2 > -1
                    && maze[current.getRow()][current.getCol() - 2] != MazePuzzle.FREE) {
                notVisited.add(new MazePoint(current.getRow(), current.getCol() - 2));
            }

            if (!notVisited.isEmpty()) {
                nextIndex = rnd.nextInt(notVisited.size());
                MazePoint p = notVisited.get(nextIndex);

                // Break the wall
                if (p.getRow() == current.getRow()) {
                    maze[p.getRow()][p.getCol()
                            + (p.getCol() < current.getCol() ? 1 : -1)] = MazePuzzle.FREE;
                } else {
                    maze[p.getRow() + (p.getRow() < current.getRow() ? 1 : -1)][p
                            .getCol()] = MazePuzzle.FREE;
                }

                currentLength++;
                if (currentLength > maxLength) {
                    if (!lowStart) {
                        maxLength = currentLength;
                        longest.setMazePoint(p);
                    } else {
                        if (p.getRow() == maze.length - 2) {
                            maxLength = currentLength;
                            longest.setMazePoint(p);
                        }
                    }
                }

                points.push(current);
                points.push(p);
            } else {
                currentLength--;
            }
        }

        return maze;
    }
}
