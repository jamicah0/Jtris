public class RotationSRS {
    public static int[][][] IRotation =
            {
                    {
                            {0, 0, 0, 0},
                            {1, 1, 1, 1},
                            {0, 0, 0, 0},
                            {0, 0, 0, 0}
                    },
                    {
                            {0, 0, 1, 0},
                            {0, 0, 1, 0},
                            {0, 0, 1, 0},
                            {0, 0, 1, 0}
                    },
                    {
                            {0, 0, 0, 0},
                            {0, 0, 0, 0},
                            {1, 1, 1, 1},
                            {0, 0, 0, 0}
                    },
                    {
                            {0, 1, 0, 0},
                            {0, 1, 0, 0},
                            {0, 1, 0, 0},
                            {0, 1, 0, 0}
                    }
            };

    public static int[][][] JRotation =
            {
                    {
                            {1, 0, 0},
                            {1, 1, 1},
                            {0, 0, 0}
                    },
                    {
                            {0, 1, 1},
                            {0, 1, 0},
                            {0, 1, 0}
                    },
                    {
                            {0, 0, 0},
                            {1, 1, 1},
                            {0, 0, 1}
                    },
                    {
                            {0, 1, 0},
                            {0, 1, 0},
                            {1, 1, 0}
                    }
            };

    public static int[][][] LRotation =
            {
                    {
                            {0, 0, 1},
                            {1, 1, 1},
                            {0, 0, 0}
                    },
                    {
                            {0, 1, 0},
                            {0, 1, 0},
                            {0, 1, 1}
                    },
                    {
                            {0, 0, 0},
                            {1, 1, 1},
                            {1, 0, 0}
                    },
                    {
                            {1, 1, 0},
                            {0, 1, 0},
                            {0, 1, 0}
                    }
            };

    public static int[][][] SRotation =
            {
                    {
                            {0, 1, 1},
                            {1, 1, 0},
                            {0, 0, 0}
                    },
                    {
                            {0, 1, 0},
                            {0, 1, 1},
                            {0, 0, 1}
                    },
                    {
                            {0, 0, 0},
                            {0, 1, 1},
                            {1, 1, 0}
                    },
                    {
                            {1, 0, 0},
                            {1, 1, 0},
                            {0, 1, 0}
                    }
            };

    public static int[][][] ZRotation =
            {
                    {
                            {1, 1, 0},
                            {0, 1, 1},
                            {0, 0, 0}
                    },
                    {
                            {0, 0, 1},
                            {0, 1, 1},
                            {0, 1, 0}
                    },
                    {
                            {0, 0, 0},
                            {1, 1, 0},
                            {0, 1, 1}
                    },
                    {
                            {1, 0, 0},
                            {1, 1, 0},
                            {0, 1, 0}
                    }
            };

    public static int[][][] TRotation =
            {
                    {
                            {0, 1, 0},
                            {1, 1, 1},
                            {0, 0, 0}
                    },
                    {
                            {0, 1, 0},
                            {0, 1, 1},
                            {0, 1, 0}
                    },
                    {
                            {0, 0, 0},
                            {1, 1, 1},
                            {0, 1, 0}
                    },
                    {
                            {0, 1, 0},
                            {1, 1, 0},
                            {0, 1, 0}
                    }
            };

    public static int[][] getRotation(Shape shape, int index) {
        switch (shape) {
            case I -> {
                return IRotation[index];
            }
            case J -> {
                return JRotation[index];
            }
            case L -> {
                return LRotation[index];
            }
            case S -> {
                return SRotation[index];
            }
            case Z -> {
                return ZRotation[index];
            }
            case T -> {
                return TRotation[index];
            }
            default -> {
                return null;
            }
        }
    }

}
