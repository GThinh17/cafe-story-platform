import Svg, {
  Circle,
  Defs,
  Ellipse,
  G,
  Path,
  RadialGradient,
  Stop,
} from "react-native-svg";

type BrandIconProps = {
  decorative?: boolean;
  size?: number;
};

export function BrandIcon({ decorative = true, size = 36 }: BrandIconProps) {
  return (
    <Svg
      accessibilityLabel={decorative ? undefined : "Cafe Story"}
      accessibilityRole={decorative ? undefined : "image"}
      height={size}
      viewBox="0 0 200 200"
      width={size}
    >
      <Defs>
        <RadialGradient cx="50%" cy="50%" id="woodGrad" r="50%">
          <Stop offset="0%" stopColor="#FFF3E3" />
          <Stop offset="70%" stopColor="#FFF3E3" />
          <Stop offset="100%" stopColor="#DBCEBE" />
        </RadialGradient>
      </Defs>

      <G>
        <Circle cx="100" cy="100" fill="url(#woodGrad)" r="95" />
        <Circle
          cx="100"
          cy="100"
          fill="none"
          r="85"
          stroke="#D4A373"
          strokeDasharray="50 15 30 15"
          strokeOpacity={0.4}
          strokeWidth={1}
        />
        <Circle
          cx="100"
          cy="100"
          fill="none"
          r="70"
          stroke="#8E5A36"
          strokeDasharray="180 30"
          strokeOpacity={0.25}
          strokeWidth={1.5}
        />
        <Circle
          cx="100"
          cy="100"
          fill="none"
          r="55"
          stroke="#3D2314"
          strokeOpacity={0.15}
          strokeWidth={1}
        />
      </G>

      <G
        fill="none"
        opacity={0.8}
        stroke="#3D2314"
        strokeLinecap="round"
        strokeWidth={2}
      >
        <Path d="M92,68 C94,56 86,47 95,34 C100,23 90,18 97,8" />
        <Path d="M108,68 C106,56 114,47 105,34 C100,23 110,18 103,8" />
      </G>

      <G>
        <Path
          d="M123,90 C138,90 138,118 123,122"
          fill="none"
          stroke="#3D2314"
          strokeLinecap="round"
          strokeWidth={3}
        />
        <Path d="M74,80 L79,132 C80,140 120,140 121,132 L126,80 Z" fill="#3D2314" />
        <Path
          d="M77,83 L81,129 C82,135 118,135 119,129 L123,83 Z"
          fill="#D4A373"
          fillOpacity={0.25}
        />
        <Ellipse cx="100" cy="80" fill="#8E5A36" rx="24" ry="5.5" />
        <Path
          d="M74,80 L79,132 C80,141 120,141 121,132 L126,80 Z"
          fill="none"
          stroke="#3D2314"
          strokeWidth={3}
        />
        <Ellipse
          cx="100"
          cy="80"
          fill="none"
          rx="26"
          ry="6"
          stroke="#3D2314"
          strokeWidth={2}
        />
      </G>

      <Path
        d="M98 78.5 C92 73 88 78 98 84.5 C108 78 104 73 98 78.5"
        fill="#FFFFFF"
        opacity={0.9}
        stroke="#3D2314"
        strokeWidth={0.5}
      />
      <G>
        <Circle cx="100" cy="100" fill="none" r="91" stroke="#3D2314" strokeWidth={2} />
        <Circle
          cx="100"
          cy="100"
          fill="none"
          r="87"
          stroke="#8E5A36"
          strokeOpacity={0.8}
          strokeWidth={0.75}
        />
      </G>
    </Svg>
  );
}
