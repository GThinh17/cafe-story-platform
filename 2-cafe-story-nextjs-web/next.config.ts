import type { NextConfig } from "next";

const cloudinaryCloudName = process.env.NEXT_PUBLIC_CLOUDINARY_CLOUD_NAME;

const nextConfig: NextConfig = {
  reactCompiler: true,
  images: {
    remotePatterns: cloudinaryCloudName
      ? [
          {
            protocol: "https",
            hostname: "res.cloudinary.com",
            pathname: `/${cloudinaryCloudName}/**`,
          },
        ]
      : [],
  },
};

export default nextConfig;
